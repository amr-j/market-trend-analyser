build:
	./mvnw package -DskipTests

run:
	./mvnw spring-boot:run

run-real:
	./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dstock.api.mode=real -Dalphavantage.api.key=$(API_KEY)"

test:
	./mvnw test

test-integration:
	./mvnw test -Dtest="MarketAnalysisControllerIT"

test-unit:
	./mvnw test -Dtest="MomentumCalculatorTest,TrendAnalyzerTest,StockApiServiceTest,MarketTrendServiceTest"