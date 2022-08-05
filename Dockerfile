FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV TIME_ZONE Asia/Shanghai
ENV LANG en_US.UTF-8
COPY classes/fonts/Alibaba-PuHuiTi-Regular.ttf /usr/share/fonts/chinese/Alibaba-PuHuiTi-Regular.ttf
RUN set -x \
&& echo "${TIME_ZONE}" > /etc/timezone \
&& mkdir -p /data/log/lookbook/shence \
&& sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories \
&& apk update \
&& apk add fontconfig \
&& apk add mkfontscale \
&& fc-cache -fv
COPY video-editor-0.0.1-SNAPSHOT.jar video-editor.jar
ENTRYPOINT ["java","-jar","/video-editor.jar"]