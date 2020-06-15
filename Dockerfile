FROM openjdk:10-jre

RUN cp /usr/share/zoneinfo/Asia/Singapore /etc/localtime
RUN echo "Asia/Singapore" >  /etc/timezone

RUN cd /
RUN mkdir -p opt
RUN cd opt 
RUN mkdir -p sku
RUN cd sku

COPY . /opt/sku/

WORKDIR /opt/sku

RUN chmod +x gradlew
RUN ./gradlew clean build

RUN cp build/libs/*.jar . 
RUN cp build/resources/main/sku-265804-e594ae1c315f.json .

CMD ["java", "-jar", "sku.jar"]

EXPOSE 8080
