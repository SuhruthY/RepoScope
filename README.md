
# RepoScope

RepoScope is a Spring Boot application designed to analyze a Git repository and provide insights into its structure, such as the number of classes, methods, interfaces, abstract classes, and usage of loops and conditionals. It provides a simple REST API to analyze a repository by cloning it and analyzing its Java files.

## Features
- Clone a repository from a given URL.
- Analyze Java files for classes, interfaces, and abstract classes.
- Count the number of methods and detect loops and conditionals within methods.
- Expose the analysis results via a REST API.

## Prerequisites
- Java 8 or later
- Maven 3.6 or later
- Git
- An IDE or command-line tools to run Spring Boot applications

## Setup and Installation

1. Clone this repository to your local machine:
    ```bash
    git clone https://github.com/yourusername/RepoScope.git
    ```

2. Navigate to the project directory:
    ```bash
    cd RepoScope
    ```

3. Build the project using Maven:
    ```bash
    mvn clean install
    ```

4. Run the application:
    ```bash
    mvn spring-boot:run
    ```

## Usage

Once the application is running, you can use the following API endpoint to analyze a repository:

### Analyze Repository

- **Endpoint**: `GET /api/repo/analyze`
- **Parameters**: `repoUrl` (URL of the Git repository you want to analyze)
- **Response**: A JSON object containing repository analysis data.

Example Request:
```bash
curl "http://localhost:8080/api/repo/analyze?repoUrl=https://github.com/spring-projects/spring-boot.git"
```

Example Response:
```json
{
  "status": "success",
  "data": {
    "repositoryInfo": {
      "repoName": "spring-boot"
    },
    "classDetails": [
      {
        "className": "Application",
        "type": "Regular Class",
        "methods": [
          {
            "name": "main",
            "containsLoops": false,
            "containsConditionals": false
          }
        ]
      }
    ],
    "methodCalls": [
      {
        "caller": "main",
        "calledMethod": "run"
      }
    ],
    "loopsAndConditionals": [],
    "summary": {
      "abstractClasses": 0,
      "interfaces": 0,
      "methodWithLoops": 0,
      "methodWithConditionals": 0
    }
  }
}
```

## Dependencies

This project uses the following dependencies:

- Spring Boot
- JGit
- JavaParser
- SLF4J (for logging)

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/SuhruthY/RepoScope/blob/master/LICENSE) file for details.
