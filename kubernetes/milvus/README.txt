cd /go/src/github.com/milvus-io/milvus

docker run --name=milvus-builder -v $PWD/.docker/amd64-ubuntu22.04-conan:/home/milvus/.conan -v .:/go/src/github.com/milvus-io/milvus -v $PWD/.docker/amd64-ubuntu22.04-ccache:/ccache -v $PWD/.docker/amd64-ubuntu22.04-go-mod:/go/pkg/mod -v $PWD/.docker/amd64-ubuntu22.04-vscode-extensions:/home/milvus/.vscode-server/extensions -e CONAN_USER_HOME=/home/milvus -e CCACHE_DIR=/ccache -e GOPATH=/go -e GOROOT=/usr/local/go -e GO111MODULE=on -e PATH=/root/.cargo/bin:/go/bin:/usr/local/go/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin -w /go/src/github.com/milvus-io/milvus -itd milvusdb/milvus-env:ubuntu22.04-20240620-5be9929 bash

export LD_PRELOAD=$PWD/internal/core/output/lib/libjemalloc.so
export ROOT_DIR=$PWD
export PKG_CONFIG_PATH="${PKG_CONFIG_PATH}:$ROOT_DIR/internal/core/output/lib/pkgconfig:$ROOT_DIR/internal/core/output/lib64/pkgconfig"
export LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:$ROOT_DIR/internal/core/output/lib:$ROOT_DIR/internal/core/output/lib64"
export RPATH=$LD_LIBRARY_PATH

go env -w GO111MODULE=on
go env -w GOPROXY=https://goproxy.cn,direct

go run cmd/main.go run mixture

go build -o milvus -gcflags=all=-N cmd/main.go

demo:
https://milvus.io/docs/quickstart.md

idea disk performance(500ops, 10ms latency):
apt install fio
mkdir test-data
fio --rw=write --ioengine=sync --fdatasync=1 --directory=test-data --size=2200m --bs=2300 --name=mytest

kubectl get storageclass

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