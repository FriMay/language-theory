#!/usr/bin/env bash

set -e

gradle clean
gradle bootJar

name=sandbox-api-app

region=eu-west-1

repo=843321185287.dkr.ecr.$region.amazonaws.com

aws ecr get-login-password | docker login --username AWS --password-stdin $repo

docker build -t $name .
docker tag $name:latest $repo/$name:latest
docker push $repo/$name:latest
