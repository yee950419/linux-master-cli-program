FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    bash \
    coreutils \
    util-linux \
    procps \
    psmisc \
    net-tools \
    iproute2 \
    iputils-ping \
    curl \
    wget \
    vim \
    nano \
    less \
    man-db \
    grep \
    gawk \
    sed \
    tar \
    gzip \
    bzip2 \
    xz-utils \
    zip \
    unzip \
    passwd \
    sudo \
    acl \
    attr \
    lsof \
    strace \
    file \
    tree \
    && rm -rf /var/lib/apt/lists/*

CMD ["tail", "-f", "/dev/null"]
