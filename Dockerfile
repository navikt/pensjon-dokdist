FROM gcr.io/distroless/java21
WORKDIR /app
COPY target/pensjon-dokdist.jar ./
EXPOSE 8080
USER nonroot
CMD ["pensjon-dokdist.jar"]