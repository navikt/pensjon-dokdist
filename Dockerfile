FROM gcr.io/distroless/java21
WORKDIR /app
COPY build/libs/pensjon-dokdist.jar ./
EXPOSE 8080
USER nonroot
CMD ["pensjon-dokdist.jar"]