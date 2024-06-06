export GO_OUT=_output/local/bin/linux/amd64

export START_MODE=nokubelet
export REUSE_CERTS=true
export ALLOW_PRIVILEGED=true

curl --retry 10 -L -o cfssl https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssl_1.5.0_linux_amd64
curl --retry 10 -L -o cfssljson https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssljson_1.5.0_linux_amd64

mv cfssl cfssljson /usr/local/bin/
chmod +x /usr/local/bin/cfssljson /usr/local/bin/cfssl

export CERT_DIR=/tmp/cert11
mkdir ${CERT_DIR}

openssl req -x509 -sha256 -new -nodes -days 365 -newkey rsa:2048 -keyout "${CERT_DIR}/server-ca.key" -out "${CERT_DIR}/server-ca.crt" -subj "/C=xx/ST=x/L=x/O=x/OU=x/CN=ca/emailAddress=x/"

echo '{"signing":{"default":{"expiry":"43800h","usages":["signing","key encipherment","server auth"]}}}' > ${CERT_DIR}/server-ca-config.json

cp ${CERT_DIR}/server-ca.key ${CERT_DIR}/client-ca.key
cp ${CERT_DIR}/server-ca.crt ${CERT_DIR}/client-ca.crt
cp ${CERT_DIR}/server-ca-config.json ${CERT_DIR}/client-ca-config.json

cd ${CERT_DIR}

// serving
echo '{"CN":"kubernetes.default","hosts":["kubernetes.default.svc","localhost","10.201.0.112","node02","10.0.0.1"],"key":{"algo":"rsa","size":2048}}' | cfssl gencert -ca=server-ca.crt -ca-key=server-ca.key -config=server-ca-config.json - | cfssljson -bare serving-kube-apiserver

mv "serving-kube-apiserver-key.pem" "serving-kube-apiserver.key"
mv "serving-kube-apiserver.pem" "serving-kube-apiserver.crt"
rm "serving-kube-apiserver.csr"

// client
kube-apiserver:
echo '{"CN":"kube-apiserver","names":[],"hosts":[""],"key":{"algo":"rsa","size":2048}}' | cfssl gencert -ca=client-ca.crt -ca-key=client-ca.key -config=client-ca-config.json - | cfssljson -bare client-kube-apiserver

mv "client-kube-apiserver-key.pem" "client-kube-apiserver.key"
mv "client-kube-apiserver.pem" "client-kube-apiserver.crt"
rm "client-kube-apiserver.csr"

create_client_certkey "" "${CERT_DIR}" 'client-ca' controller system:kube-controller-manager

create_signing_certkey "" "${CERT_DIR}" request-header '"client auth"'
create_client_certkey "" "${CERT_DIR}" request-header-ca auth-proxy system:auth-proxy

inspect crt:
echo "xxx" | base64 -d > /tmp/client.crt
openssl x509 -in /tmp/client.crt -text

nohup etcd --advertise-client-urls http://0.0.0.0:2379 --data-dir /opt/k8s/etcd_data --listen-client-urls http://0.0.0.0:2379 --log-level=warn 2> "/tmp/etcd.log" >/dev/null &

kube-apiserver:
$GO_OUT/kube-apiserver --authorization-mode=Node,RBAC  --cloud-provider= --cloud-config=   --v=3 --vmodule= --audit-policy-file=/tmp/kube-audit-policy-file --audit-log-path=/tmp/kube-apiserver-audit.log --authorization-webhook-config-file= --authentication-token-webhook-config-file= --cert-dir=${CERT_DIR} --egress-selector-config-file=/tmp/kube_egress_selector_configuration.yaml --client-ca-file=${CERT_DIR}/client-ca.crt --kubelet-client-certificate=${CERT_DIR}/client-kube-apiserver.crt --kubelet-client-key=${CERT_DIR}/client-kube-apiserver.key --service-account-key-file=/tmp/kube-serviceaccount.key --service-account-lookup=true --service-account-issuer=https://kubernetes.default.svc --service-account-jwks-uri=https://kubernetes.default.svc/openid/v1/jwks --service-account-signing-key-file=/tmp/kube-serviceaccount.key --enable-admission-plugins=NamespaceLifecycle,LimitRanger,ServiceAccount,DefaultStorageClass,DefaultTolerationSeconds,Priority,MutatingAdmissionWebhook,ValidatingAdmissionWebhook,ResourceQuota,NodeRestriction --disable-admission-plugins= --admission-control-config-file= --bind-address=0.0.0.0 --secure-port=6443 --tls-cert-file=${CERT_DIR}/serving-kube-apiserver.crt --tls-private-key-file=${CERT_DIR}/serving-kube-apiserver.key --storage-backend=etcd3 --storage-media-type=application/vnd.kubernetes.protobuf --etcd-servers=http://127.0.0.1:2379 --service-cluster-ip-range=10.0.0.0/24 --feature-gates=AllAlpha=false --external-hostname=localhost --requestheader-username-headers=X-Remote-User --requestheader-group-headers=X-Remote-Group --requestheader-extra-headers-prefix=X-Remote-Extra- --requestheader-client-ca-file=${CERT_DIR}/request-header-ca.crt --requestheader-allowed-names=system:auth-proxy --proxy-client-cert-file=${CERT_DIR}/client-auth-proxy.crt --proxy-client-key-file=${CERT_DIR}/client-auth-proxy.key --cors-allowed-origins="/127.0.0.1(:[0-9]+)?$,/localhost(:[0-9]+)?$" > "/tmp/apiserver.log" 2>&1 &

kube-controller:
$GO_OUT/kube-controller-manager --v=3 --vmodule= --service-account-private-key-file=/tmp/kube-serviceaccount.key --service-cluster-ip-range=10.0.0.0/24 --root-ca-file=/var/run/kubernetes/server-ca.crt --cluster-signing-cert-file=/var/run/kubernetes/client-ca.crt --cluster-signing-key-file=/var/run/kubernetes/client-ca.key --enable-hostpath-provisioner=false --pvclaimbinder-sync-period=15s --feature-gates=AllAlpha=false --cloud-provider= --cloud-config= --configure-cloud-routes=true --authentication-kubeconfig /var/run/kubernetes/controller.kubeconfig --authorization-kubeconfig /var/run/kubernetes/controller.kubeconfig --kubeconfig /var/run/kubernetes/controller.kubeconfig --use-service-account-credentials --controllers=* --leader-elect=false --cert-dir=/var/run/kubernetes --master=https://localhost:6443 > "/tmp/kube-controller.log" 2>&1 &

kube-scheduler:
./kube-scheduler --v=3 --config=/tmp/kube-scheduler.yaml --feature-gates=AllAlpha=false --authentication-kubeconfig /var/run/kubernetes/scheduler.kubeconfig --authorization-kubeconfig /var/run/kubernetes/scheduler.kubeconfig --master=https://localhost:6443 > "/tmp/kube-scheduler.log" 2>&1 &

alias kubectl='/opt/k8s/kubectl --kubeconfig "/var/run/kubernetes/admin.kubeconfig"'

./kubectl --kubeconfig "/var/run/kubernetes/admin.kubeconfig" create clusterrolebinding kube-apiserver-kubelet-admin --clusterrole=system:kubelet-api-admin --user=kube-apiserver

./kubectl --kubeconfig "/var/run/kubernetes/admin.kubeconfig" create clusterrolebinding kubelet-csr --clusterrole=system:certificates.k8s.io:certificatesigningrequests:selfnodeclient --group=system:nodes

kubelet:
cni:
/opt/cni
/opt/cni/bin/loopback
./kubelet --v=3 --vmodule= --container-runtime=remote --hostname-override=127.0.0.1 --cloud-provider= --cloud-config= --bootstrap-kubeconfig=/var/run/kubernetes/kubelet.kubeconfig --container-runtime-endpoint=unix:///run/containerd/containerd.sock --kubeconfig=/var/run/kubernetes/kubelet-rotated.kubeconfig --config=/tmp/kubelet.yaml > "/tmp/kubelet.log" 2>&1 &

/run/docker/libcontainerd/docker-containerd.sock

test:
kubectl create deployment nginx --image=nginx
kubectl expose deployment nginx --port=80 --type=NodePort