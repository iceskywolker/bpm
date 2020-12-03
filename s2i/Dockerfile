# springboot-centos7
FROM openshift/base-centos7

RUN echo "Starting build..."


# TODO: Put the maintainer name in the image metadata
LABEL maintainer="Tomasz Chmiel <tchmiel@man.pcz.pl>"

# TODO: Rename the builder environment variable to inform users about application you provide them

# Java jdk 11, Maven 3.6.3, Gradle 6.7.1
ENV GRADLE_VERSION 6.7.1
ENV MAVEN_VERSION 3.6.3
ENV JDK_VERSION 11

ENV BUILDER_VERSION 1.0



# TODO: Set labels used in OpenShift to describe the builder image
#LABEL io.k8s.description="Platform for building xyz" \
#      io.k8s.display-name="builder x.y.z" \
#      io.openshift.expose-services="8888:http" \
#      io.openshift.tags="builder,x.y.z,etc."

LABEL io.k8s.description="Platform for building Spring Boot applications with maven or gradle" \
      io.k8s.display-name="Spring Boot builder 1.0" \
      io.openshift.expose-services="8888:http" \
      io.openshift.tags="builder,maven-3,gradle-6.7.1,springboot"


#io.openshift.s2i.destination
# TODO: Install required packages here:
# RUN yum install -y ... && yum clean all -y

RUN yum install -y --enablerepo=centosplus \
    tar unzip bc which lsof java-11-openjdk java-11-openjdk-devel && \
    yum clean all -y

RUN (curl -0 https://downloads.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | \
    tar -zx -C /usr/local) && \
    mv /usr/local/apache-maven-$MAVEN_VERSION /usr/local/maven && \
    ln -sf /usr/local/maven/bin/mvn /usr/local/bin/mvn && \
    curl -sL -0 https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip /tmp/gradle-${GRADLE_VERSION}-bin.zip -d /usr/local/ && \
    rm /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    mv /usr/local/gradle-${GRADLE_VERSION} /usr/local/gradle && \
    ln -sf /usr/local/gradle/bin/gradle /usr/local/bin/gradle && \
    mkdir -p /opt/openshift && \
    mkdir -p /opt/app-root/source && chmod -R a+rwX /opt/app-root/source && \
    mkdir -p /opt/s2i/destination && chmod -R a+rwX /opt/s2i/destination && \
    mkdir -p /opt/app-root/src && chmod -R a+rwX /opt/app-root/src

ENV PATH=/opt/maven/bin/:/opt/gradle/bin/:$PATH


#RUN yum install -y rubygems && yum clean all -y
#RUN gem install  TODO (optional): Copy the builder files into /opt/app-root
# COPY ./<builder_folder>/ /opt/app-root/

# TODO: Copy the S2I scripts to /usr/libexec/s2i, since openshift/base-centos7 image
# sets io.openshift.s2i.scripts-url label that way, or update that label
# COPY ./s2i/bin/ /usr/libexec/s2i


LABEL io.openshift.s2i.scripts-url=image:///usr/local/s2i
COPY ./s2i/bin/ /usr/local/s2i

#LABEL io.openshift.s2i.scripts-url=image:///usr/libexec/s2i
#COPY ./s2i/bin/ image:///usr/libexec/s2i

RUN chown -R 1001:1001 /opt/openshift
RUN chown -R 1001:1001 /opt/app-root

# TODO: Drop the root user and make the content of /opt/app-root owned by user 1001
# RUN chown -R 1001:1001 /opt/app-root

# This default user is created in the openshift/base-centos7 image
USER 1001

# TODO: Set the default port for applications built using this image
EXPOSE 8888

# TODO: Set the default CMD for the image
# CMD ["/usr/libexec/s2i/usage"]
CMD ["usage"]