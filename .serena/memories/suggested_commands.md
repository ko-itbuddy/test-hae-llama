# Suggested Commands

## Python Core Engine
- **Install Dependencies**: `pip install -r requirements.txt`
- **Ingest Sample Project**: `python src/main.py ingest --project-path sample-project`
- **Generate Test**: `python src/main.py generate --target-file sample-project/src/main/java/com/example/demo/service/OrderService.java --project-path sample-project`
- **Run Engine Tests**: `pytest src/tests/test_engine.py`

## Sample Java Project
- **Compile Tests**: `gradle -p sample-project compileTestJava`
- **Run Tests**: `gradle -p sample-project test`

## Environment Check
- **Check Models**: `ollama list`
- **Java Version**: `javac -version`
