create_signing_certkey() {
    local sudo=$1
    local dest_dir=$2
    local id=$3
    echo "signing id:" ${id}
    local purpose=$4
    # Create client ca
    echo "purpose:" $purpose
    ${sudo} /usr/bin/env bash -e <<EOF
    rm -f "${dest_dir}/${id}-ca.crt" "${dest_dir}/${id}-ca.key"
    openssl req -x509 -sha256 -new -nodes -days 365 -newkey rsa:2048 -keyout "${dest_dir}/${id}-ca.key" -out "${dest_dir}/${id}-ca.crt" -subj "/C=xx/ST=x/L=x/O=x/OU=x/CN=ca/emailAddress=x/"
    echo '{"signing":{"default":{"expiry":"43800h","usages":["signing","key encipherment",${purpose}]}}}' > "${dest_dir}/${id}-ca-config.json"
EOF
}

function create_client_certkey() {
    local sudo=$1
    local dest_dir=$2
    local ca=$3
    echo "client ca:" $ca
    local id=$4
    echo "client id:" $id
    local cn=${5:-$4}
    echo "client cn:" $cn
    local groups=""
    local SEP=""
    shift 5
    while [ -n "${1:-}" ]; do
        groups+="${SEP}{\"O\":\"$1\"}"
        SEP=","
        shift 1
    done
    echo "groups:" $groups
    ${sudo} /usr/bin/env bash -e <<EOF
    cd ${dest_dir}
    echo '{"CN":"${cn}","names":[${groups}],"hosts":[""],"key":{"algo":"rsa","size":2048}}' | cfssl gencert -ca=${ca}.crt -ca-key=${ca}.key -config=${ca}-config.json - | cfssljson -bare client-${id}
    mv "client-${id}-key.pem" "client-${id}.key"
    mv "client-${id}.pem" "client-${id}.crt"
    rm -f "client-${id}.csr"
EOF
}

function create_serving_certkey {
    local sudo=$1
    local dest_dir=$2
    local ca=$3
    local id=$4
    local cn=${5:-$4}
    local hosts=""
    local SEP=""
    shift 5
    while [ -n "${1:-}" ]; do
        hosts+="${SEP}\"$1\""
        SEP=","
        shift 1
    done
    echo "hosts:" ${hosts}
    ${sudo} /usr/bin/env bash -e <<EOF
    cd ${dest_dir}
    echo '{"CN":"${cn}","hosts":[${hosts}],"key":{"algo":"rsa","size":2048}}' | cfssl gencert -ca=${ca}.crt -ca-key=${ca}.key -config=${ca}-config.json - | cfssljson -bare serving-${id}
    mv "serving-${id}-key.pem" "serving-${id}.key"
    mv "serving-${id}.pem" "serving-${id}.crt"
    rm -f "serving-${id}.csr"
EOF
}

write_client_kubeconfig() {
    local sudo=$1
    local dest_dir=$2
    local ca_file=$3
    local api_host=$4
    local api_port=$5
    local client_id=$6
    local token=${7:-}
    cat <<EOF | ${sudo} tee "${dest_dir}"/"${client_id}".kubeconfig > /dev/null
apiVersion: v1
kind: Config
clusters:
  - cluster:
      certificate-authority: ${ca_file}
      server: https://${api_host}:${api_port}/
    name: local-up-cluster
users:
  - user:
      token: ${token}
      client-certificate: ${dest_dir}/client-${client_id}.crt
      client-key: ${dest_dir}/client-${client_id}.key
    name: local-up-cluster
contexts:
  - context:
      cluster: local-up-cluster
      user: local-up-cluster
    name: local-up-cluster
current-context: local-up-cluster
EOF
    username=$(whoami)
    ${sudo} /usr/bin/env bash -e <<EOF
    kubectl --kubeconfig="${dest_dir}/${client_id}.kubeconfig" config view --minify --flatten > "/tmp/${client_id}.kubeconfig"
    mv -f "/tmp/${client_id}.kubeconfig" "${dest_dir}/${client_id}.kubeconfig"
    chown ${username} "${dest_dir}/${client_id}.kubeconfig"
EOF
}