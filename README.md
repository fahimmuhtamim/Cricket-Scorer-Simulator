# Cricket Scorer Simulator

A comprehensive JavaFX application for managing cricket matches and tournaments with real-time scoring capabilities.

## Overview

The Cricket Scorer Simulator is a desktop application built with JavaFX that allows users to create, manage, and score cricket matches and tournaments. The application provides an intuitive interface for tracking live matches, maintaining tournament standings, and generating detailed match statistics.

## Features

### User Management
- User authentication with login/signup functionality
- Guest access for quick usage
- Secure user session management

### Tournament Management
- Create and manage multiple tournaments
- View tournament history
- Tournament dashboard with match overview
- Comprehensive point table system with team rankings

### Match Management
- Create new matches with team selection
- Support for multiple match formats
- Live match scoring interface
- Real-time score updates and statistics
- Player-wise scoring and bowling figures

### Scoring Features
- Ball-by-ball scoring interface
- Multiple scoring options: runs (0-6), wickets, wides, byes, no balls
- Undo functionality for score corrections
- Live score display with overs and wickets
- Economy rate calculations for bowlers, strike rate calculation for batters

### Statistics & Analytics
- Detailed match summaries
- Player performance statistics
- Team standings and net run rate calculations
- Match result tracking
- Tournament point tables with win/loss records

## System Requirements

- Java 8 or higher
- JavaFX Runtime (included in Java 8, separate download for Java 11+)
- Minimum 512MB RAM
- 50MB free disk space

## Installation

1. Ensure Java 8+ is installed on your system
2. Download the Cricket Scorer Simulator JAR file
3. If using Java 11+, ensure JavaFX modules are available
4. Run the application using: `java -jar CricketScorerSimulator.jar`

## Usage

### Getting Started
1. Launch the application
2. Choose to login with existing credentials, sign up for a new account, or continue as guest
3. Access the Tournament Manager from the main menu

### Creating a Tournament
1. Click "Create New Tournament" from the Tournament Manager
2. Enter tournament details and settings
3. Add participating teams
4. Save the tournament configuration

### Managing Matches
1. Select a tournament from the Previous Tournaments list
2. Click "New Match" to create a match between teams
3. Configure match settings (overs, teams, players)
4. Use the live scoring interface to update scores ball-by-ball

### Live Scoring
1. Select the current bowler and batsman
2. Use the scoring buttons to record each ball:
   - Number buttons (0-6) for runs scored
   - "Wicket" for dismissals
   - "Wide" for wide balls
   - "By" for byes
   - "No Ball" for no balls
3. Use "Undo" to correct any mistakes
4. Innings is switched automatically when the first team's innings is complete

### Viewing Results
1. Access match details to see complete scorecards
2. View point tables for tournament standings
3. Check team rankings and net run rates
4. Review match overviews and statistics

## File Structure

```
CricketScorerSimulator/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/cricketscorer/
│   │   │       ├── sync/
│   │   │       │   └── networking
│   │   │       ├── controllers/
│   │   │       ├── views/
│   │   │       └── utils/
│   │   └── resources/
│   │       ├── com/example/cricketscorer/fxml
│   │       └── images/
├── tournaments.txt
├── Users.txt
├── Passwrods.txt
└── README.txt
```

## Data Storage

The application stores data locally in the following formats:
- User accounts and authentication data
- Tournament configurations and settings
- Match data including ball-by-ball records
- Player statistics and performance data
- Team information and standings

## Troubleshooting

### Common Issues

**Application won't start:**
- Verify Java installation: `java -version`
- Ensure JavaFX is available (for Java 11+)
- Check if JAR file is corrupted

**Login issues:**
- Use "View as Guest" for immediate access
- Verify username/password combination
- Try creating a new account

**Scoring interface problems:**
- Use "Undo" button to correct mistakes
- Ensure proper player selection before scoring
- Restart match if serious data corruption occurs

**Tournament/Match not saving:**
- Check file permissions in application directory
- Ensure sufficient disk space
- Verify data directory structure

## Technical Details

### Architecture
- Built using JavaFX for cross-platform GUI
- MVC (Model-View-Controller) design pattern
- Local file-based data persistence
- Object-oriented design with cricket-specific models

### Key Components
- Tournament Manager: Handles tournament creation and management
- Match Engine: Core scoring and match logic
- Statistics Calculator: Computes rankings, averages, and rates
- User Interface: Responsive JavaFX-based GUI

## Version History

### Version 1.0
- Initial release with basic scoring functionality
- Tournament and match management
- User authentication system
- Point table generation

## Support

For technical support or feature requests:
1. Check the troubleshooting section above
2. Review application logs for error details
3. Ensure you're using a supported Java version
4. Contact the development team with specific error messages

## License

This software is provided as-is for cricket scoring and tournament management purposes.

## Credits

Developed for cricket enthusiasts who need a reliable scoring system for matches and tournaments.

---

Last Updated: July 2025
Application Version: 1.0
