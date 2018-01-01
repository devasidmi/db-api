FROM ubuntu:17.04

# Update packages
RUN apt-get -y update

# Install postgresql
ENV PGVER 9.6
RUN apt-get install -y postgresql-$PGVER

USER postgres

RUN /etc/init.d/postgresql start &&\
    psql --command "CREATE USER admin WITH SUPERUSER PASSWORD '1234567890';" &&\
    createdb -O admin docker &&\
    /etc/init.d/postgresql stop

RUN echo "synchronous_commit = off" >>  /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "fsync = off" >> /etc/postgresql/$PGVER/main/postgresql.conf


#EXPOSE PORT
EXPOSE 5432

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

USER root

RUN apt-get install -y openjdk-8-jdk-headless maven

ENV PGUSER=admin PGPASSWORD=1234567890 PGHOST=127.0.0.1 PGPORT=5432 PGDATABASE=docker
ENV PARK_DB_ROOT=/var/www/docker

RUN mkdir -p $PARK_DB_ROOT
COPY . $PARK_DB_ROOT
WORKDIR $PARK_DB_ROOT

RUN mvn package

EXPOSE 5000

CMD service postgresql start && java -Xmx300M -Xms300M -jar $PARK_DB_ROOT/target/ForumDB-1.0-SNAPSHOT.jar