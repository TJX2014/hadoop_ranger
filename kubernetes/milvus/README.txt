cd /go/src/github.com/milvus-io/milvus

docker run --name=milvus-debug1 -p 19531:19530 -p 2380:2379 -p 9092:9091 -v $PWD/.docker/amd64-ubuntu22.04-conan:/home/milvus/.conan -v .:/go/src/github.com/milvus-io/milvus -v $PWD/.docker/amd64-ubuntu22.04-ccache:/ccache -v $PWD/.docker/amd64-ubuntu22.04-go-mod:/go/pkg/mod -v $PWD/.docker/amd64-ubuntu22.04-vscode-extensions:/home/milvus/.vscode-server/extensions -e CONAN_USER_HOME=/home/milvus -e CCACHE_DIR=/ccache -e GOPATH=/go -e GOROOT=/usr/local/go -e GO111MODULE=on -e PATH=/root/.cargo/bin:/go/bin:/usr/local/go/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin -w /go/src/github.com/milvus-io/milvus -itd milvusdb/milvus-env:ubuntu22.04-20240620-5be9929 bash

export LD_PRELOAD=$PWD/internal/core/output/lib/libjemalloc.so
export ROOT_DIR=$PWD
export PKG_CONFIG_PATH="${PKG_CONFIG_PATH}:$ROOT_DIR/internal/core/output/lib/pkgconfig:$ROOT_DIR/internal/core/output/lib64/pkgconfig"
export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:$ROOT_DIR/internal/core/output/lib:$ROOT_DIR/internal/core/output/lib64"
export RPATH=$LD_LIBRARY_PATH

go env -w GO111MODULE=on
go env -w GOPROXY=https://goproxy.cn,direct

go run cmd/main.go run mixture

go build -o bin/milvus -gcflags=all=-N cmd/main.go

demo:
https://milvus.io/docs/quickstart.md

idea disk performance(500ops, 10ms latency):
apt install fio
mkdir test-data
fio --rw=write --ioengine=sync --fdatasync=1 --directory=test-data --size=2200m --bs=2300 --name=mytest

kubectl get storageclass

set default sc:
k8s32compute patch storageclass local-path -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'

cat <<EOF > test-nfs-fio-pvc.yaml
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: test-nfs-fio-pvc
spec:
  storageClassName: local-path
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
EOF

cat <<EOF > test-nfs-fio-pod.yaml
kind: Pod
apiVersion: v1
metadata:
  name: test-nfs-fio-pod
spec:
  containers:
  - name: test-nfs-fio-pod
    image: node02:5000/openebs/tests-fio:latest
    command:
      - "/bin/sh"
    args:
      - "-c"
      - "touch /mnt/SUCCESS && sleep 86400"
    volumeMounts:
      - name: nfs-fio-pvc
        mountPath: "/mnt"
  restartPolicy: "Never"
  volumes:
    - name: nfs-fio-pvc
      persistentVolumeClaim:
        claimName: test-nfs-fio-pvc
EOF

kubectl apply -f test-nfs-fio-pod.yaml

docker pull quay.io/jetstack/cert-manager-webhook:v1.5.3 
docker pull quay.io/jetstack/cert-manager-cainjector:v1.5.3 
docker pull quay.io/jetstack/cert-manager-controller:v1.5.3

kubectl apply -f https://raw.githubusercontent.com/zilliztech/milvus-operator/main/deploy/manifests/deployment.yaml

kubectl port-forward svc/kafka-milvus6-milvus --address 0.0.0.0 19530:19530

cluster milvus setup:
export LD_LIBRARY_PATH=/go/src/github.com/milvus-io/milvus/lib:/usr/lib

mixcoord:
bin/milvus run mixture -rootcoord -querycoord -datacoord -indexcoord

dlv exec --headless --api-version=2 --listen=:2345 bin/milvus -- run mixture -rootcoord -querycoord -datacoord -indexcoord

datanode:
bin/milvus run datanode

bash -c 'export ETCD_USE_EMBED=true && export COMMON_STORAGETYPE=local && milvus run standalone'

kubectl -n default create deployment milvus-alone --image=node02:5000/milvus:latest -- bash -c 'export ETCD_USE_EMBED=true && export COMMON_STORAGETYPE=local && milvus run standalone'

kubectl expose deploy milvus-alone --port 19531 --target-port 19530