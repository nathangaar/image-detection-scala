![Build Status](https://github.com/nathangaar/image-detection-scala/actions/workflows/scala.yml/badge.svg)

# Image Detection Scala

This project is a Scala application that interacts with the Imagga API for image analysis and detection. It supports image metadata management and database interactions.

## Features

- Image metadata storage and retrieval
- Integration with image detection APIs
- RESTful web service for image-related operations

## Technologies

- **Scala**: A functional programming language that promotes immutability and type safety.
- **HTTP4s**: A purely functional library for building REST APIs, emphasizing immutability and composable route definitions.
- **Doobie**: A functional database library that provides type-safe interactions while managing side effects, allowing database queries to be expressed as pure functions.
- **Circe**: A JSON library that automatically derives encoders and decoders, facilitating seamless JSON and Scala case class conversions using immutable structures.
- **PostgreSQL**: A relational database for storing image metadata, accessed functionally via Doobie


## Prerequisites

Before running this application, ensure you have the following installed:

- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) (version 11 or higher)
- [sbt](https://www.scala-sbt.org/download.html) (Scala Build Tool)

## Setup

1. **Install Java:**
   - Follow the instructions on the [Java installation page](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) for your operating system.
  
2. **Install sbt:**
   - Follow the instructions on the [sbt installation page](https://www.scala-sbt.org/download.html).
   - For macOS users, you can use Homebrew:
     ```bash
     brew install sbt
     ```

3. **Clone the repository:**
   ```bash
   git clone git@github.com:nathangaar/image-detection-scala.git
   cd image-detection-scala
   ```

4. **Configure your environment:**
   - Create a `.env` file in the root directory of the project and add the necessary environment variables (like API keys, database credentials, etc.).

## Running the App

To run the application, follow these steps:

1. **Navigate to the project directory:**
   ```bash
   cd image-detection-scala
   ```

2. **Start the sbt console:**
   ```bash
   sbt
   ```

3. **Run the application:**
   In the sbt console, enter:
   ```scala
   run
   ```
## Run specs
  ```bash
  sbt test
  ```  

## API Endpoints

- **GET /images?objects=dogs,cats   Retrieve images from db that are associated with these terms
- **GET /images                     Retrieve images from  database.
- **GET /images/:id                 Retrieve image from the database.
- **POST /images    {"imageUrl": "example.com", label: "optional_label", "imageDetectionEnabled": true} 
                                     Analyze an image using the Imagga API.



