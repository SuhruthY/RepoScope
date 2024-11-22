
# RepoScope: Repository Analysis Tool

RepoScope is a Spring Boot application designed to analyze Java repositories hosted on GitHub or other Git servers. It clones a repository, processes the Java files, and provides insights on various aspects of the codebase such as class types, methods, loops, conditionals, and method calls.

## Features

- **Clone Repositories**: Clone any public Git repository using its URL.
- **Analyze Code**: Process Java files to gather statistics such as:
  - Total number of classes and methods
  - Number of interfaces and abstract classes
  - Methods containing loops and conditionals
  - Method call analysis (which methods are calling other methods)
- **RESTful API**: Expose analysis results through a REST API for integration with other services.

## Requirements

- Java 8 or higher
- Maven or Gradle (Gradle is used in this project)
- Git
- JGit library for cloning repositories
- JavaParser for analyzing Java code

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/repository-name.git
   ```

2. Navigate to the project directory:
   ```bash
   cd repository-name
   ```

3. Build the project using Gradle:
   ```bash
   ./gradlew build
   ```

4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

5. The application will start a Spring Boot server on port `8080` by default.

## Usage

The application exposes a REST API endpoint to analyze repositories:

### Endpoint: `/api/repo/analyze`

- **Method**: `GET`
- **Parameters**: 
  - `repoUrl`: The URL of the GitHub (or any Git repository) repository to analyze. (e.g., `https://github.com/suhruth/repository-name`)

- **Response**: A JSON object containing the following data:
  - `repositoryInfo`: Information about the repository (e.g., name).
  - `classDetails`: A list of details for each class in the repository (class name, type, methods).
  - `methodCalls`: A list of method call relationships.
  - `loopsAndConditionals`: A list of methods that contain loops and conditionals.
  - `summary`: A summary of the repository (e.g., number of interfaces, abstract classes, methods with loops).

Example Request:
```bash
curl "http://localhost:8080/api/repo/analyze?repoUrl=https://github.com/yourusername/repository-name"
```

### Example Response:
```json
{
  "status": "success",
  "data": {
    "repositoryInfo": {
      "repoName": "repository-name"
    },
    "classDetails": [
      {
        "className": "ExampleClass",
        "type": "Regular Class",
        "methods": [
          {
            "name": "exampleMethod",
            "containsLoops": true,
            "containsConditionals": false
          }
        ]
      }
    ],
    "methodCalls": [
      {
        "caller": "exampleMethod",
        "calledMethod": "helperMethod"
      }
    ],
    "loopsAndConditionals": [
      {
        "method": "exampleMethod",
        "loopType": "for loop"
      }
    ],
    "summary": {
      "abstractClasses": 1,
      "interfaces": 2,
      "methodWithLoops": 3,
      "methodWithConditionals": 2
    }
  }
}
```

## Project Structure

- **`src/main/java/com/suhruth/reposcope/`**: Contains the main application, controller, and service classes.
  - **`RepoScopeApplication.java`**: The entry point of the Spring Boot application.
  - **`controller/RepositoryAnalysisController.java`**: Exposes the `/api/repo/analyze` API endpoint.
  - **`service/RepositoryAnalysisService.java`**: Contains the core logic for cloning repositories, analyzing Java files, and generating reports.

- **`src/main/resources/`**:
  - **`clonedRepo/`**: Temporary directory where repositories are cloned for analysis.

## Technologies Used

- **Spring Boot**: Framework for building the REST API and running the application.
- **JGit**: Library for cloning and interacting with Git repositories.
- **JavaParser**: Used to parse and analyze Java files.
- **SLF4J** with **Logback**: Logging framework used to track the application's progress and errors.

## Contributing

1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Push to the feature branch.
5. Open a pull request.

Please make sure to update the README and add tests if applicable.

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/SuhruthY/RepoScope/blob/master/LICENSE) file for details.
