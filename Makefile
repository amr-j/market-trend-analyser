build:
	./mvnw package -DskipTests

run:
	./mvnw spring-boot:run

run-real:
	./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dstock.api.mode=real -Dalphavantage.api.key=$(API_KEY)"

test:
	./mvnw test

integration-test:
	docker-compose up -d
	sleep 5
	./mvnw test -Dtest="*IT"
	docker-compose down

test-unit:
	./mvnw test -Dtest="MomentumCalculatorTest,TrendAnalyzerTest,StockApiServiceTest,MarketTrendServiceTest"