# ThymeLab Launcher

ThymeLab Launcher is a desktop application that provides a user-friendly interface for running and managing the ThymeLab Processor. It eliminates the need for manual Java environment setup and command-line operations.

## Features

### Easy Setup and Operation
- **One-Click Installation**: Simple installer for Windows and macOS
- **No Java Setup Required**: Bundled JVM eliminates manual Java environment configuration
- **Visual Directory Configuration**: Easy setup of template, static resource, and data directories
- **Server Control**: Start/stop processor with a single click
- **Port Configuration**: Customize server port through UI

### Real-time Monitoring
- **Live Log Display**: View processor logs in real-time
- **Log Level Control**: Adjust log detail level on the fly
- **Log Buffer Management**: Configure log history size
- **Clear Log Function**: Clean log display with one click

### User Interface
- **System Tray Integration**: Quick access to common functions
- **Modern UI**: Clean interface with FlatLaf theme
- **Internationalization**: English and Korean language support
- **Font Customization**: Adjustable log display font and size

## Installation

### Windows
1. Download `ThymeLab-x.x.x.exe`
2. Run the installer
3. Follow the installation wizard
4. Launch from Start Menu or Desktop shortcut

### macOS
1. Download `ThymeLab-x.x.x.dmg`
2. Open the DMG file
3. Drag ThymeLab to Applications folder
4. Launch from Applications

## Quick Start Guide

1. **First Launch**
   - Start ThymeLab from your applications menu
   - The main window shows server status and log display

2. **Configure Directories**
   - Click Settings in the menu
   - Set paths for:
     - Templates Directory (Thymeleaf templates)
     - Static Resources Directory (CSS, JS, images)
     - Data Directory (JSON files)

3. **Start Development**
   - Click Start to run the processor
   - Open `http://localhost:8080` in your browser
   - Edit templates and see changes in real-time

## Settings

### Server Configuration
- **Port**: Change server port (default: 8080)
- **Log Level**: Set logging detail level
  - ERROR
  - WARN
  - INFO
  - DEBUG

### Display Settings
- **Language**: Switch between English and Korean
- **Font**: Customize log display font
- **Font Size**: Adjust log text size
- **Log Buffer**: Set maximum log lines to display

### Directory Settings
- **Templates**: Location of Thymeleaf template files
- **Static Resources**: Location of web resources
- **Data**: Location of JSON data files

## System Tray Features
- Minimize to system tray
- Show/hide main window
- Exit application

## Integration with ThymeLab Processor
- Automatically manages processor lifecycle
- Provides real-time log feedback
- Handles configuration changes
- Ensures proper startup and shutdown

## System Requirements
- Windows 10/11 or macOS 10.14+
- No additional Java installation required

## Support
For issues and questions, please create an issue in the GitHub repository. 