# CivicChain - Blockchain-Enabled Voting System

A secure, transparent, and decentralized voting platform built with blockchain technology. CivicChain enables fair and tamper-proof elections through a combination of modern web technologies and distributed ledger architecture.

## 🌟 Features

- **Blockchain Integration**: Immutable voting records secured on blockchain
- **Secure Authentication**: OTP-based voter verification system
- **Admin Dashboard**: Comprehensive election management interface
- **Real-time Results**: Live voting results and analytics
- **Voter Privacy**: Anonymized voting while maintaining accountability
- **Candidate Management**: Easy creation and management of candidates
- **Multi-user Support**: Admin, candidates, and voter role management

## 🛠️ Tech Stack

### Frontend
- **HTML5** - Semantic markup and structure
- **CSS3** - Modern responsive styling
- **JavaScript** - Interactive client-side functionality
- **Bootstrap/Custom CSS** - Responsive design framework

### Backend
- **Spring Boot 3.3.2** - Java-based REST API framework
- **Java 17** - Latest stable Java version
- **MySQL** - Relational database for storing voting data
- **Spring Data JPA** - Object-relational mapping and database operations
- **Maven** - Build and dependency management

### Architecture
- **Microservices Pattern** - Modular service-based design
- **RESTful APIs** - Stateless API endpoints
- **MVC Pattern** - Clear separation of concerns

## 📋 Project Structure

```
CIVICCHAIN/
├── backend/
│   └── civicpulse/                    # Spring Boot Backend
│       ├── src/
│       │   └── main/java/com/example/civicpulse/
│       │       ├── controller/        # REST API Controllers
│       │       │   ├── AdminController.java
│       │       │   ├── VoterController.java
│       │       │   ├── CandidateController.java
│       │       │   └── VotingController.java
│       │       ├── service/           # Business Logic Services
│       │       │   ├── AdminService.java
│       │       │   ├── VotingService.java
│       │       │   ├── CandidateService.java
│       │       │   ├── BlockService.java
│       │       │   └── OtpService.java
│       │       └── entity/            # JPA Entities
│       ├── pom.xml                    # Maven Dependencies
│       ├── mvnw                       # Maven Wrapper (Linux/Mac)
│       └── mvnw.cmd                   # Maven Wrapper (Windows)
├── frontend/
│   ├── index.html                     # Home/Dashboard Page
│   ├── login.html                     # Voter Login
│   ├── vote.html                      # Voting Interface
│   ├── results.html                   # Results Display
│   ├── admin.html                     # Admin Dashboard
│   └── js/                            # JavaScript Files
├── patch_login.py                     # Utility Script
├── LICENSE                            # MIT License
└── todo.md                            # Development Notes
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- MySQL 5.7 or higher
- Maven 3.6+
- Modern web browser (Chrome, Firefox, Safari, Edge)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/SOUMYAJIT200515/CIVICCHAIN.git
   cd CIVICCHAIN/backend/civicpulse
   ```

2. **Configure Database**
   - Create a MySQL database for the project
   - Update database credentials in `application.properties` or `application.yml`
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/civicchain
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

3. **Build the Project**
   ```bash
   # Using Maven Wrapper (recommended)
   ./mvnw clean install
   
   # Or using Maven directly
   mvn clean install
   ```

4. **Run the Application**
   ```bash
   # Using Maven Wrapper
   ./mvnw spring-boot:run
   
   # Or using Maven directly
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Navigate to Frontend Directory**
   ```bash
   cd CIVICCHAIN/frontend
   ```

2. **Serve the Frontend**
   - Option A: Use a local web server
     ```bash
     python -m http.server 3000
     # Or with Python 3
     python3 -m http.server 3000
     ```
   
   - Option B: Use Node.js http-server
     ```bash
     npx http-server -p 3000
     ```

3. **Access the Application**
   - Open `http://localhost:3000` in your browser

## 📡 API Endpoints

### Authentication
- `POST /api/voter/register` - Register new voter
- `POST /api/voter/login` - Voter login with credentials
- `POST /api/otp/send` - Send OTP for verification
- `POST /api/otp/verify` - Verify OTP

### Voting
- `GET /api/voting/candidates` - Get list of candidates
- `POST /api/voting/cast-vote` - Cast a vote
- `GET /api/voting/results` - Get current voting results
- `GET /api/voting/voter-status` - Check if voter has voted

### Admin
- `POST /api/admin/create-election` - Create new election
- `POST /api/admin/add-candidate` - Add candidate to election
- `GET /api/admin/election-stats` - Get election statistics
- `POST /api/admin/finalize-election` - End election and finalize results

### Candidate
- `GET /api/candidates` - List all candidates
- `GET /api/candidates/{id}` - Get candidate details
- `POST /api/candidates/register` - Register as candidate

### Blockchain
- `POST /api/block/add` - Add vote to blockchain
- `GET /api/block/chain` - Retrieve blockchain
- `GET /api/block/verify` - Verify blockchain integrity

## 🔐 Security Features

- **OTP-Based Authentication**: One-time passwords for voter verification
- **Password Hashing**: Secure password storage with encryption
- **Blockchain Immutability**: Tamper-proof voting records
- **Role-Based Access Control**: Different permissions for admins, candidates, and voters
- **Session Management**: Secure user session handling
- **Input Validation**: Server-side validation of all inputs

## 📊 Database Schema

### Key Tables
- **voters** - Registered voters with authentication details
- **candidates** - Election candidates and their information
- **votes** - Voting records linked to voters and candidates
- **elections** - Election metadata and status
- **blocks** - Blockchain blocks containing vote data
- **otps** - OTP records for authentication

## 🎯 Key Components

### Frontend Pages

- **index.html** - Home page and user dashboard
- **login.html** - Voter authentication interface
- **vote.html** - Voting page with candidate selection
- **results.html** - Election results and statistics
- **admin.html** - Admin control panel for election management

### Backend Services

- **VotingService** - Core voting logic and vote recording
- **BlockService** - Blockchain operations and integrity verification
- **OtpService** - OTP generation and validation
- **CandidateService** - Candidate management
- **AdminService** - Administrative operations

## 🧪 Testing

Run the test suite with Maven:
```bash
mvn test
```

## 📝 Development

### Code Style
- Follow Java naming conventions
- Use meaningful variable names
- Add comments for complex logic
- Keep methods focused and single-purpose

### Adding Features
1. Create feature branch: `git checkout -b feature/your-feature`
2. Implement changes with tests
3. Commit: `git commit -m "Add: brief description"`
4. Push: `git push origin feature/your-feature`

## 🐛 Known Issues & Limitations

- Current version uses centralized database (roadmap includes full decentralization)
- OTP delivery via email requires SMTP configuration
- Blockchain features are partially implemented
- Frontend requires JavaScript enabled

## 🗺️ Roadmap

- [ ] Full blockchain implementation for all voting records
- [ ] Multi-election support
- [ ] Enhanced analytics and visualization
- [ ] Mobile application
- [ ] Automated deployment pipeline
- [ ] Advanced cryptography integration
- [ ] Voter audit trails
- [ ] Multi-language support

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 Soumyajit Saha

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

## 👤 Author

**Soumyajit Saha**
- GitHub: [@SOUMYAJIT200515](https://github.com/SOUMYAJIT200515)

## 📧 Contact & Support

For questions, issues, or suggestions:
- Open an issue on GitHub
- Check existing documentation
- Review code comments for implementation details

## 🙏 Acknowledgments

- Spring Boot framework and community
- MySQL for reliable database management
- Contributors and testers

---

**Note:** This is a demonstration/educational project showcasing voting system concepts with blockchain integration. Always conduct proper security audits before using in production environments.
