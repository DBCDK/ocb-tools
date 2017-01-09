FROM docker-i.dbc.dk/dbc-java8:latest
MAINTAINER Team Iscrum, iscrum@dbc.dk

ENV OCB_USER ocbtools
ENV OCB_USER_HOME /home/$OCB_USER
RUN useradd $OCB_USER && \
    mkdir -p $OCB_USER_HOME/bin

ENV LOG_DIR /data/logs
RUN mkdir -p ${LOG_DIR}
RUN mkdir -p $OCB_USER_HOME/results

RUN mkdir -p /opencat-business/target/surefire-reports
ENV PATH $PATH:$OCB_USER_HOME/bin

ADD http://is.dbc.dk/job/ocb-tools-head/lastSuccessfulBuild/artifact/target/dist/ocb-tools-1.0.0.tar.gz $OCB_USER_HOME/
ADD http://is.dbc.dk/job/opencat-business-head/lastSuccessfulBuild/artifact/deploy/opencat-business.tar.gz $OCB_USER_HOME/
ADD scripts/bin/*.sh $OCB_USER_HOME/bin/

RUN cd $OCB_USER_HOME && \
    tar xf ocb-tools-1.0.0.tar.gz && \
    tar xf opencat-business.tar.gz && \
    cd $OCB_USER_HOME/bin && \
    ln -s ../ocb-tools-1.0.0/bin/ocb-record.sh . && \
    ln -s ../ocb-tools-1.0.0/bin/ocb-test.sh . && \
    chmod +x $OCB_USER_HOME/bin/*.sh && \
    chown -R $OCB_USER:$OCB_USER $OCB_USER_HOME && \
    chown -R $OCB_USER:$OCB_USER $LOG_DIR

USER $OCB_USER
EXPOSE 8080
