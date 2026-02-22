package za.ac.za.cput.prac50.domain;

/**
 * Venue class - Complete version with all getters, setters, and utility methods
 * Represents study venue locations with capacity management
 * * @author LearnHub Team - Complete Version
 */
public class Venue {
    private int venueId;               // Database ID
    private String name;
    private int capacity;
    private int currentUsage;
    private String[] studyTypes;
    private String operatingHours;
    private String[] amenities;
    private double latitude;           // For map integration
    private double longitude;          // For map integration
    
    // ===== CONSTRUCTORS =====
    
    /**
     * Default constructor
     */
    public Venue() {
        this.venueId = 0;
        this.name = "";
        this.capacity = 0;
        this.currentUsage = 0;
        this.studyTypes = new String[0];
        this.operatingHours = "";
        this.amenities = new String[0];
        this.latitude = 0.0;
        this.longitude = 0.0;
    }
    
    /**
     * Constructor with basic fields (original version)
     */
    public Venue(String name, int capacity, int currentUsage, String[] studyTypes, 
                String operatingHours, String[] amenities) {
        this.name = name;
        this.capacity = capacity;
        this.currentUsage = currentUsage;
        this.studyTypes = studyTypes;
        this.operatingHours = operatingHours;
        this.amenities = amenities;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }
    
    /**
     * Constructor with all fields including coordinates
     */
    public Venue(int venueId, String name, int capacity, int currentUsage, 
                String[] studyTypes, String operatingHours, String[] amenities,
                double latitude, double longitude) {
        this.venueId = venueId;
        this.name = name;
        this.capacity = capacity;
        this.currentUsage = currentUsage;
        this.studyTypes = studyTypes;
        this.operatingHours = operatingHours;
        this.amenities = amenities;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // ===== GETTERS =====
    
    public int getVenueId() {
        return venueId;
    }
    
    public String getName() { 
        return name; 
    }
    
    public int getCapacity() { 
        return capacity; 
    }
    
    public int getCurrentUsage() { 
        return currentUsage; 
    }
    
    public String[] getStudyTypes() { 
        return studyTypes; 
    }
    
    public String getOperatingHours() { 
        return operatingHours; 
    }
    
    public String[] getAmenities() { 
        return amenities; 
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    // ===== SETTERS =====
    
    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }
    
    /**
     * Set the venue name
     * @param name Venue name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Set the venue capacity
     * @param capacity Maximum number of seats
     */
    public void setCapacity(int capacity) {
        if (capacity > 0) {
            this.capacity = capacity;
            // Adjust current usage if it now exceeds new capacity
            if (this.currentUsage > capacity) {
                this.currentUsage = capacity;
            }
        } else {
            System.err.println("Warning: Attempted to set invalid capacity. Ignoring.");
        }
    }
    
    /**
     * Set the current usage count
     * @param currentUsage Number of currently occupied seats
     */
    public void setCurrentUsage(int currentUsage) {
        // Validate that usage doesn't exceed capacity
        if (currentUsage >= 0 && currentUsage <= capacity) {
            this.currentUsage = currentUsage;
        } else if (currentUsage > capacity) {
            this.currentUsage = capacity; // Cap at maximum capacity
            System.err.println("Warning: Attempted to set usage (" + currentUsage + 
                             ") exceeding capacity (" + capacity + "). Capped at capacity.");
        } else {
            this.currentUsage = 0; // Don't allow negative values
            System.err.println("Warning: Attempted to set negative usage. Set to 0.");
        }
    }
    
    /**
     * Set the study types available at this venue
     * @param studyTypes Array of study type strings
     */
    public void setStudyTypes(String[] studyTypes) {
        this.studyTypes = studyTypes;
    }
    
    /**
     * Set the operating hours
     * @param operatingHours Hours string (e.g., "07:00 - 22:00")
     */
    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }
    
    /**
     * Set the amenities available at this venue
     * @param amenities Array of amenity strings
     */
    public void setAmenities(String[] amenities) {
        this.amenities = amenities;
    }
    
    /**
     * Set the latitude coordinate
     * @param latitude Latitude coordinate
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    /**
     * Set the longitude coordinate
     * @param longitude Longitude coordinate
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // ===== UTILITY METHODS =====
    
    /**
     * Get available seats
     * @return Number of available seats
     */
    public int getAvailableSeats() {
        return capacity - currentUsage;
    }

    /**
     * Check if venue has available space
     * @return true if seats available
     */
    public boolean isAvailable() {
        return getAvailableSeats() > 0;
    }

    /**
     * Check if venue is at capacity
     * @return true if fully booked
     */
    public boolean isFull() {
        return currentUsage >= capacity;
    }

    /**
     * Get occupancy percentage
     * @return Percentage of seats occupied (0-100)
     */
    public double getOccupancyPercentage() {
        if (capacity == 0) return 0.0;
        return (currentUsage * 100.0) / capacity;
    }

    /**
     * Book a spot at this venue (increment usage)
     * @return true if booking successful, false if venue is full
     */
    public boolean bookSpot() {
        if (isAvailable()) {
            currentUsage++;
            return true;
        }
        return false;
    }

    /**
     * Book multiple spots at this venue
     * @param count Number of spots to book
     * @return true if booking successful, false if not enough space
     */
    public boolean bookSpots(int count) {
        if (getAvailableSeats() >= count && count > 0) {
            currentUsage += count;
            return true;
        }
        return false;
    }

    /**
     * Release a spot at this venue (decrement usage)
     * @return true if release successful, false if usage is already 0
     */
    public boolean releaseSpot() {
        if (currentUsage > 0) {
            currentUsage--;
            return true;
        }
        return false;
    }

    /**
     * Release multiple spots at this venue
     * @param count Number of spots to release
     * @return true if release successful, false if not enough occupied spots
     */
    public boolean releaseSpots(int count) {
        if (currentUsage >= count && count > 0) {
            currentUsage -= count;
            return true;
        }
        return false;
    }

    /**
     * Reset current usage to 0
     */
    public void resetUsage() {
        this.currentUsage = 0;
    }
    
    // ===== PART 4: Add method to check if coordinates are set =====

    /**
     * Check if venue has valid coordinates
     * @return true if latitude and longitude are set
     */
    public boolean hasValidCoordinates() {
        return latitude != 0.0 && longitude != 0.0;
    }
    
    /**
     * Check if venue supports a specific study type
     * @param studyType Study type to check
     * @return true if supported
     */
    public boolean supportsStudyType(String studyType) {
        if (studyTypes == null || studyType == null) return false;
        for (String type : studyTypes) {
            if (type.equalsIgnoreCase(studyType.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if venue has a specific amenity
     * @param amenity Amenity to check
     * @return true if available
     */
    public boolean hasAmenity(String amenity) {
        if (amenities == null || amenity == null) return false;
        for (String a : amenities) {
            if (a.equalsIgnoreCase(amenity.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
 * Get study types as a comma-separated string
 * @return String of study types
 */
public String getStudyTypesAsString() {
    if (studyTypes == null || studyTypes.length == 0) {
        return "No study types specified";
    }
    return String.join(", ", studyTypes);
}

/**
 * Get amenities as a comma-separated string
 * @return String of amenities
 */
public String getAmenitiesAsString() {
    if (amenities == null || amenities.length == 0) {
        return "No amenities specified";
    }
    return String.join(", ", amenities);
}
    
    // hashCode, equals, toString, and validation methods (omitted for brevity, assume they are correct)
    
    @Override
    public String toString() {
        return "Venue{" +
                "venueId=" + venueId +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", currentUsage=" + currentUsage +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Venue venue = (Venue) o;
        return name != null && name.equalsIgnoreCase(venue.name);
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : 0;
    }
    
    // ===== VALIDATION =====
    
    /**
     * Validate venue data
     * @return true if all required fields are valid
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               capacity > 0 &&
               currentUsage >= 0 &&
               currentUsage <= capacity &&
               operatingHours != null && !operatingHours.trim().isEmpty();
    }
    
    /**
     * Get validation errors
     * @return List of validation error messages, empty if valid
     */
    public java.util.List<String> getValidationErrors() {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            errors.add("Venue name is required");
        }
        if (capacity <= 0) {
            errors.add("Capacity must be greater than 0");
        }
        if (currentUsage < 0) {
            errors.add("Current usage cannot be negative");
        }
        if (currentUsage > capacity) {
            errors.add("Current usage cannot exceed capacity");
        }
        if (operatingHours == null || operatingHours.trim().isEmpty()) {
            errors.add("Operating hours are required");
        }
        
        return errors;
    }
}