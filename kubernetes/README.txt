export GO_OUT=_output/local/bin/linux/amd64

export START_MODE=nokubelet
export REUSE_CERTS=true
export ALLOW_PRIVILEGED=true

curl --retry 10 -L -o cfssl https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssl_1.5.0_linux_amd64
curl --retry 10 -L -o cfssljson https://github.com/cloudflare/cfssl/releases/download/v1.5.0/cfssljson_1.5.0_linux_amd64

mv cfssl cfssljson /usr/local/bin/

chmod +x /usr/local/bin/cfssljson /usr/local/bin/cfssl

nohup etcd --advertise-client-urls http://0.0.0.0:2379 --data-dir /opt/k8s/etcd_data --listen-client-urls http://0.0.0.0:2379 --log-level=warn 2> "/tmp/etcd.log" >/dev/null &

kube-apiserver:
$GO_OUT/kube-apiserver --authorization-mode=Node,RBAC  --cloud-provider= --cloud-config=   --v=3 --vmodule= --audit-policy-file=/tmp/kube-audit-policy-file --audit-log-path=/tmp/kube-apiserver-audit.log --authorization-webhook-config-file= --authentication-token-webhook-config-file= --cert-dir=/var/run/kubernetes --egress-selector-config-file=/tmp/kube_egress_selector_configuration.yaml --client-ca-file=/var/run/kubernetes/client-ca.crt --kubelet-client-certificate=/var/run/kubernetes/client-kube-apiserver.crt --kubelet-client-key=/var/run/kubernetes/client-kube-apiserver.key --service-account-key-file=/tmp/kube-serviceaccount.key --service-account-lookup=true --service-account-issuer=https://kubernetes.default.svc --service-account-jwks-uri=https://kubernetes.default.svc/openid/v1/jwks --service-account-signing-key-file=/tmp/kube-serviceaccount.key --enable-admission-plugins=NamespaceLifecycle,LimitRanger,ServiceAccount,DefaultStorageClass,DefaultTolerationSeconds,Priority,MutatingAdmissionWebhook,ValidatingAdmissionWebhook,ResourceQuota,NodeRestriction --disable-admission-plugins= --admission-control-config-file= --bind-address=0.0.0.0 --secure-port=6443 --tls-cert-file=/var/run/kubernetes/serving-kube-apiserver.crt --tls-private-key-file=/var/run/kubernetes/serving-kube-apiserver.key --storage-backend=etcd3 --storage-media-type=application/vnd.kubernetes.protobuf --etcd-servers=http://127.0.0.1:2379 --service-cluster-ip-range=10.0.0.0/24 --feature-gates=AllAlpha=false --external-hostname=localhost --requestheader-username-headers=X-Remote-User --requestheader-group-headers=X-Remote-Group --requestheader-extra-headers-prefix=X-Remote-Extra- --requestheader-client-ca-file=/var/run/kubernetes/request-header-ca.crt --requestheader-allowed-names=system:auth-proxy --proxy-client-cert-file=/var/run/kubernetes/client-auth-proxy.crt --proxy-client-key-file=/var/run/kubernetes/client-auth-proxy.key --cors-allowed-origins="/127.0.0.1(:[0-9]+)?$,/localhost(:[0-9]+)?$" > "/tmp/apiserver.log" 2>&1 &

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