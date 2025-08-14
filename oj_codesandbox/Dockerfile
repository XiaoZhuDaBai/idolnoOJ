FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY Problems/L0001 /app
COPY UserCode/8290afb0-e769-4017-8391-5ec374da48e3/Main.class /app
CMD ["sh", "-c", "java -Xmx256m -Dfile.encoding=UTF-8 -cp /app Main < input/input2.txt"]