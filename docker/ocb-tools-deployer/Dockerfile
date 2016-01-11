FROM java:latest
MAINTAINER Sune Thomas Poulsen, stp@dbc.dk

ENV JAVA_HOME         /usr/lib/jvm/java-8-openjdk-amd64
ENV OCB_USER    ocbtools
ENV OCB_USER_HOME /home/$OCB_USER

ENV PATH /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:$JAVA_HOME/bin:$OCB_USER_HOME/bin

RUN apt-get update && \
    apt-get install -y expect procps curl unzip zip inotify-tools less && \
    rm -rf /var/lib/apt/lists/*

# Create user to run ocb-tools
RUN useradd $OCB_USER && \
    mkdir $OCB_USER_HOME && \
    mkdir -p $OCB_USER_HOME/bin

ADD artifacts/ocb-tools-1.0.0.tgz $OCB_USER_HOME/
ADD artifacts/opencat-business.tgz $OCB_USER_HOME/
ADD scripts/bin/*.sh $OCB_USER_HOME/bin/

RUN mkdir -p $OCB_USER_HOME/bin && \
    cd $OCB_USER_HOME/bin && \
    ln -s ../ocb-tools-1.0.0/bin/ocb-record.sh . && \
    ln -s ../ocb-tools-1.0.0/bin/ocb-test.sh . && \
    chmod ug+x $OCB_USER_HOME/bin/*.sh && \
    chown -R $OCB_USER:$OCB_USER $OCB_USER_HOME

USER $OCB_USER
WORKDIR $OCB_USER_HOME

EXPOSE 8080
