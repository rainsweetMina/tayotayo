
echo "wait db server"
dockerize -wait tcp://database:3306 -timeout 20s

echo "start server"

java -jar /app/app.jar
