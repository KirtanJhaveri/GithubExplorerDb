# Highly Parallel Actor-Based Framework for Extracting a snapshot of GitHub using GraphQL

## Project Overview

GitHub repositories are of paramount importance in software development. GitHub
repositories not only simplify the lifecycle and continuous development process for
developers but also act as datasets for researchers. These researchers search repositories
to extract patterns and trends that drive new libraries, frameworks, and improvements
for existing or obsolete code bases. This project is a solution to automate GitHub
repository retrieval by user-defined criteria. This application was developed using Scala,
Akka, Cassandra, GraphQL, ZIO, and Caliban. The application provides a solution for
efficient and scalable extraction tasks. This framework handles large datasets by
combining Akka's actor modeling for operations and GraphQL's ability to streamline
responses. This combination of technologies meets the needs of software researchers and
developers by enabling them to efficiently retrieve data related to their custom
requirements while being scalable to handle data from multiple repositories at the same
time. This report justifies the technology selection, implementation details, limitations,
and possible future enhancements of the framework, highlighting its impact on software
engineering research.

If you're interested in learning more about this project please refer the [report](Kirtan_MS_Project_Report.pdf) in the repository.

## Project Structure

### Main Components

- **Main Application (`Main` object):**
    - Initializes the Actor System.
    - Sets up a shutdown hook to close resources.
    - Sends a start message to the Root Actor.
    - Handles Actor System termination explicitly.

- **Root Actor (`RootActor` object):**
    - Manages repository and issue fetching workflows.
    - Spawns child actors (`RepoActor` and `IssueFetchActor`).
    - Processes messages such as `Start`, `RepoActorReply`, and `IssueFetchActorReply`.

- **Repository Actor (`RepoActor` object):**
    - Sends queries to fetch repository information.
    - Replies to `RootActor` with repository data.

- **Issue Fetch Actor (`IssueFetchActor` object):**
    - Fetches issues for a given repository.
    - Stores issues and related commit data in Cassandra.
    - Replies to `RootActor` upon completion.

- **Queries (`RepoQuery` and `IssueQuery` objects):**
    - Define and execute GraphQL queries to fetch repository and issue data from GitHub.

- **Utilities:**
    - **CassandraClient:** Manages Cassandra session and database operations.
    - **HttpUtil:** Utility to send HTTP requests using sttp client.

### Key Classes and Objects

- **RootActor:**
    - `Message`: Sealed trait representing messages handled by the Root Actor.
    - `Start`: Case class to initiate the process.
    - `RepoActorReply`: Case class to handle repository actor replies.
    - `IssueFetchActorReply`: Case class to handle issue fetch actor replies.

- **RepoActor:**
    - `Message`: Sealed trait representing messages handled by the Repo Actor.
    - `MessageReceived`: Case class to process received messages.

- **IssueFetchActor:**
    - `Command`: Sealed trait representing commands handled by the Issue Fetch Actor.
    - `FetchIssues`: Case class to initiate issue fetching.

- **RepoQuery:**
    - Defines a GraphQL query to fetch repository information based on specified criteria.
    - Uses ZIO effects to handle asynchronous operations.

- **IssueQuery:**
    - Defines a GraphQL query to fetch issues for a specific repository.
    - Uses ZIO effects to handle asynchronous operations.

### Configuration

The project uses a configuration file (`application.conf`) to manage:
- GitHub GraphQL endpoint.
- GitHub OAuth token.
- Search language and the number of results.

### Dependencies

- **Scala(v2.13.13)**
- **sbt (v1.9,6)**
- **Akka Typed(v2.9.2):** For actor-based concurrency.
- **Caliban(v2.5.1):** For GraphQL client functionality.
- **ZIO(v2.1.0):** For managing asynchronous and concurrent operations.
- **sttp:** For HTTP client capabilities.
- **Cassandra:** For database storage.
- **Docker(Optional):** To run a Cassandra instance


## Getting Started

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-repo/github-explorer.git
   cd github-explorer
   ```
2. **Configure the application:**
    - Update `application.conf` with your GitHub OAuth token and other parameters.

3. **Start the Cassandra instance and ensure it is running. Installation,table creation and connection steps have been explained in a [section](Installing%20and%20Connecting%20Cassandra%20with%20the%20application) below**.

4. **Build and run the application using sbt:**
   ```bash
   sbt run
   ```
    The application fetches repository and issue data from GitHub based on the specified criteria in the configuration file. It processes the data and stores it in a Cassandra database.

#### Shutdown Hook
The application includes a shutdown hook to ensure proper resource cleanup:

- Closes the Cassandra session.
- Terminates the Actor System.

#### Logging
The application uses `slf4j` for logging. Logs provide information about:

- Actor lifecycle events.
- Query execution results.
- Database operations.

## Installing and Connecting Cassandra with the application
#### _**I containerised cassandra using Docker, I've explained steps below(If you already have a cassandra instance running skip directly to the "creating tables" step)**_
1. **Pull the Cassandra Docker image:**

    ```bash
    docker pull cassandra:latest
    ```

2. **Run a Cassandra container:**

    ```bash
    docker run --name cassandra -d cassandra:latest
    ```

3. **Verify Cassandra is running:**

    ```bash
    docker ps
    ```
### Creating the Tables

1. **Access the Cassandra container:**

    ```bash
    docker exec -it cassandra cqlsh
    ```

2. **Create the keyspace and tables:**

    ```sql
    -- Create a keyspace
    CREATE KEYSPACE github_data WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

    -- Use the keyspace
    USE github_data;

    -- Create repositories table
    CREATE TABLE repositories (
        repo_name TEXT,
        owner_name TEXT,
        disk_usage INT,
        has_issues_enabled BOOLEAN,
        is_archived BOOLEAN,
        is_disabled BOOLEAN,
        is_empty BOOLEAN,
        primary_language TEXT,
        repo_url TEXT,
        PRIMARY KEY (repo_name, owner_name)
    );

    -- Create issues table
    CREATE TABLE issues (
        owner_name TEXT,
        repo_name TEXT,
        issue_id TEXT,
        title TEXT,
        body TEXT,
        PRIMARY KEY (owner_name, repo_name, issue_id)
    );

    -- Create commits table
    CREATE TABLE commits (
        owner_name TEXT,
        repo_name TEXT,
        issue_id TEXT,
        commit_id TEXT,
        commit_message TEXT,
        PRIMARY KEY (owner_name, repo_name, issue_id, commit_id)
    );
    ```
### Connecting the Application to Cassandra

Ensure the application is configured to connect to Cassandra. Update the `application.conf` file with the correct connection details:
Below is the configuration I used, customise to match yours.
```
cassandra {
  contact-points = ["127.0.0.1"]
  keyspace = "github_data"
  port = 9042
} 
```

## Contributing
### Contributions are welcome! Please follow the guidelines:
1. **Fork the repository.**
2. **Create a feature branch.**
3. **Commit your changes.**
4. **Push to the branch.**
5. **Create a pull request.**





