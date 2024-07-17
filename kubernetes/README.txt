export GO_OUT=_output/local/bin/linux/amd64
export GO_OUT=_output/local/bin/darwin/amd64/

make -C . WHAT="cmd/kubectl cmd/kube-apiserver cmd/kube-controller-manager cmd/cloud-controller-manager cmd/kubelet cmd/kube-proxy cmd/kube-scheduler"

make DBG=1 GOFLAGS=-v -C . WHAT="cmd/kubelet"

-gcflags= all=-N -l-asmflags=-ldflags=all=-X
:~/kubernetes/_output/local/go/src# go install k8s.io/kubernetes/cmd/kubelet

go build -o kubelet_darwin -gcflags=all=-N k8s.io/kubernetes/cmd/kubelet
GOOS=linux GOARCH=amd64 go build -o kubelet_linux -gcflags=all=-N k8s.io/kubernetes/cmd/kubelet

export START_MODE=nokubelet
export REUSE_CERTS=true
export ALLOW_PRIVILEGED=true

curl --retry 10 -L -o cfssl https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssl_1.5.0_linux_amd64
curl --retry 10 -L -o cfssljson https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssljson_1.5.0_linux_amd64

mv cfssl cfssljson /usr/local/bin/
chmod +x /usr/local/bin/cfssljson /usr/local/bin/cfssl

nohup etcd --advertise-client-urls http://0.0.0.0:2379 --data-dir /tmp/etcd_data --listen-client-urls http://0.0.0.0:2379 --log-level=warn 2> "/tmp/etcd.log" >/dev/null &

cat <<EOF > /tmp/kube_egress_selector_configuration.yaml
apiVersion: apiserver.k8s.io/v1beta1
kind: EgressSelectorConfiguration
egressSelections:
- name: cluster
  connection:
    proxyProtocol: Direct
- name: controlplane
  connection:
    proxyProtocol: Direct
- name: etcd
  connection:
    proxyProtocol: Direct
EOF

kube-apiserver:
export SERVICE_ACCOUNT_KEY=/tmp/kube-serviceaccount.key
openssl genrsa -out "${SERVICE_ACCOUNT_KEY}" 2048 2>/dev/null

cat <<EOF > /tmp/kube_egress_selector_configuration.yaml
apiVersion: apiserver.k8s.io/v1beta1
kind: EgressSelectorConfiguration
egressSelections:
- name: cluster
  connection:
    proxyProtocol: Direct
- name: controlplane
  connection:
    proxyProtocol: Direct
- name: etcd
  connection:
    proxyProtocol: Direct
EOF

cat <<EOF > /tmp/kube-audit-policy-file
# Log all requests at the Metadata level.
apiVersion: audit.k8s.io/v1
kind: Policy
rules:
- level: Metadata
EOF

$GO_OUT/kube-apiserver --authorization-mode=Node,RBAC  --cloud-provider= --cloud-config=   --v=3 --vmodule= --audit-policy-file=/tmp/kube-audit-policy-file --audit-log-path=/tmp/kube-apiserver-audit.log --authorization-webhook-config-file= --authentication-token-webhook-config-file= --cert-dir=${CERT_DIR} --egress-selector-config-file=/tmp/kube_egress_selector_configuration.yaml --client-ca-file=${CERT_DIR}/client-ca.crt --kubelet-client-certificate=${CERT_DIR}/client-kube-apiserver.crt --kubelet-client-key=${CERT_DIR}/client-kube-apiserver.key --service-account-key-file=${SERVICE_ACCOUNT_KEY} --service-account-lookup=true --service-account-issuer=https://kubernetes.default.svc --service-account-jwks-uri=https://kubernetes.default.svc/openid/v1/jwks --service-account-signing-key-file=${SERVICE_ACCOUNT_KEY} --enable-admission-plugins=NamespaceLifecycle,LimitRanger,ServiceAccount,DefaultStorageClass,DefaultTolerationSeconds,Priority,MutatingAdmissionWebhook,ValidatingAdmissionWebhook,ResourceQuota,NodeRestriction --disable-admission-plugins= --admission-control-config-file= --bind-address=0.0.0.0 --secure-port=6443 --tls-cert-file=${CERT_DIR}/serving-kube-apiserver.crt --tls-private-key-file=${CERT_DIR}/serving-kube-apiserver.key --storage-backend=etcd3 --storage-media-type=application/vnd.kubernetes.protobuf --etcd-servers=http://127.0.0.1:2379 --service-cluster-ip-range=12.0.0.1/24 --feature-gates=AllAlpha=false --external-hostname=localhost --requestheader-username-headers=X-Remote-User --requestheader-group-headers=X-Remote-Group --requestheader-extra-headers-prefix=X-Remote-Extra- --requestheader-client-ca-file=${CERT_DIR}/request-header-ca.crt --requestheader-allowed-names=system:auth-proxy --proxy-client-cert-file=${CERT_DIR}/client-auth-proxy.crt --proxy-client-key-file=${CERT_DIR}/client-auth-proxy.key --cors-allowed-origins="/127.0.0.1(:[0-9]+)?$,/localhost(:[0-9]+)?$" > "/tmp/apiserver.log" 2>&1 &

kube-controller:
$GO_OUT/kube-controller-manager --v=3 --vmodule= --service-account-private-key-file=${SERVICE_ACCOUNT_KEY} --service-cluster-ip-range=12.0.0.0/24 --root-ca-file=${CERT_DIR}/server-ca.crt --cluster-signing-cert-file=${CERT_DIR}/client-ca.crt --cluster-signing-key-file=${CERT_DIR}/client-ca.key --enable-hostpath-provisioner=false --pvclaimbinder-sync-period=15s --feature-gates=AllAlpha=false --cloud-provider= --cloud-config= --configure-cloud-routes=true --authentication-kubeconfig ${CERT_DIR}/controller.kubeconfig --authorization-kubeconfig ${CERT_DIR}/controller.kubeconfig --kubeconfig ${CERT_DIR}/controller.kubeconfig --use-service-account-credentials --controllers=* --leader-elect=false --cert-dir=${CERT_DIR} --master=https://localhost:6443 > "/tmp/kube-controller.log" 2>&1 &

kube-scheduler:
cat <<EOF > /tmp/kube-scheduler.yaml
apiVersion: kubescheduler.config.k8s.io/v1beta2
kind: KubeSchedulerConfiguration
clientConnection:
  kubeconfig: ${CERT_DIR}/scheduler.kubeconfig
leaderElection:
  leaderElect: false
EOF

$GO_OUT/kube-scheduler --v=3 --config=/tmp/kube-scheduler.yaml --feature-gates=AllAlpha=false --authentication-kubeconfig ${CERT_DIR}/scheduler.kubeconfig --authorization-kubeconfig ${CERT_DIR}/scheduler.kubeconfig --master=https://localhost:6443 > "/tmp/kube-scheduler.log" 2>&1 &

alias kubectl='/opt/k8s/kubectl --kubeconfig "/var/run/kubernetes/admin.kubeconfig"'

./kubectl --kubeconfig "${CERT_DIR}/admin.kubeconfig" create clusterrolebinding kube-apiserver-kubelet-admin --clusterrole=system:kubelet-api-admin --user=kube-apiserver

./kubectl --kubeconfig "${CERT_DIR}/admin.kubeconfig" create clusterrolebinding kubelet-csr --clusterrole=system:certificates.k8s.io:certificatesigningrequests:selfnodeclient --group=system:nodes

kubelet:
cni:
/opt/cni
/opt/cni/bin/loopback

export HOSTNAME_OVERRIDE=node02
rm -fr "/var/lib/kubelet/pki" "${CERT_DIR}/kubelet-rotated.kubeconfig"

ifconfig
ifconfig cni0 down
ip link delete cni0

sudo $GO_OUT/kubelet --v=3 --vmodule= --container-runtime=remote --hostname-override=${HOSTNAME_OVERRIDE} --cloud-provider= --cloud-config= --bootstrap-kubeconfig=${CERT_DIR}/kubelet_${HOSTNAME_OVERRIDE}.kubeconfig --container-runtime-endpoint=unix:///run/containerd/containerd.sock --kubeconfig=${CERT_DIR}/kubelet-rotated.kubeconfig --config=/tmp/kubelet.yaml > "/tmp/kubelet.log" 2>&1 &

kubectl get csr

sudo dlv --listen=:2345 --headless=true --api-version=2 exec $GO_OUT/kubelet -- --v=3 --vmodule= --container-runtime=remote --hostname-override=${HOSTNAME_OVERRIDE} --cloud-provider= --cloud-config= --bootstrap-kubeconfig=${CERT_DIR}/kubelet_${HOSTNAME_OVERRIDE}.kubeconfig --container-runtime-endpoint=unix:///run/containerd/containerd.sock --kubeconfig=${CERT_DIR}/kubelet-rotated.kubeconfig --config=/tmp/kubelet.yaml

open ip_forward:
vim /etc/sysctl.conf
net.ipv4.ip_forward = 1

refresh:
sysctl --system | grep forward

kube-proxy:
cat <<EOF > /tmp/kube-proxy_${HOSTNAME_OVERRIDE}.yaml
apiVersion: kubeproxy.config.k8s.io/v1alpha1
kind: KubeProxyConfiguration
clientConnection:
  kubeconfig: ${CERT_DIR}/kube-proxy_${HOSTNAME_OVERRIDE}.kubeconfig
hostnameOverride: ${HOSTNAME_OVERRIDE}
mode: "ipvs"
conntrack:
# Skip setting sysctl value "net.netfilter.nf_conntrack_max"
  maxPerCore: 0
# Skip setting "net.netfilter.nf_conntrack_tcp_timeout_established"
  tcpEstablishedTimeout: 0s
# Skip setting "net.netfilter.nf_conntrack_tcp_timeout_close"
  tcpCloseWaitTimeout: 0s
EOF

"${GO_OUT}/kube-proxy" \
      --v=3 \
      --config=/tmp/kube-proxy_${HOSTNAME_OVERRIDE}.yaml \
      --master="https://node02:6443" > "/tmp/kube-proxy.log" 2>&1 &

/run/docker/libcontainerd/docker-containerd.sock

test:
kubectl create deployment nginx --image=nginx
kubectl expose deployment nginx --port=80 --type=NodePort
kubectl delete deployment nginx

kubectl -n kube-system create deployment busybox --image=node02:5000/busybox:latest -- sleep 3600

kubectl -n kube-system set image deployment/metrics-server metrics-server=node02:5000/metrics-server:v3

kubectl -n kube-system create deployment busybox --image=registry.cn-hangzhou.aliyuncs.com/google_containers/busybox:latest -- date

kubectl debug -n kube-system -it metrics-server-7865fc7db6-fvnwp --copy-to=mydebug --container=metrics-server --image=node02:5000/metrics-server:v3 -- sleep 36000

registry.cn-hangzhou.aliyuncs.com/google_containers/metrics-server:v0.7.1

/metrics-server --cert-dir=/tmp --secure-port=10250 --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname --kubelet-use-node-status-port --metric-resolution=15s

/metrics-server --kubelet-insecure-tls --bind-address=$(POD_IP) --cert-dir=/tmp --secure-port=10250 --kubelet-preferred-address-types=InternalIP,Hostname --kubelet-use-node-status-port --metric-resolution=15s

--tls-cert-file=/tmp/apiserver.crt --tls-private-key-file=/tmp/apiserver.key

curl -v --cacert /tmp/apiserver.key --cert /tmp/apiserver.crt https://10.0.0.1:443/api/v1/namespaces/kube-system/configmaps/extension-apiserver-authentication

curl -v https://12.0.0.1:443/api/v1/namespaces/kube-system/configmaps/extension-apiserver-authentication

curl -v --cacert /tmp/cert11/server-ca.crt https://node02:6443/version

# failed to verify certificate: x509: certificate signed by unknown authority
cp ${CERT_DIR}/server-ca.crt /etc/ssl/certs/