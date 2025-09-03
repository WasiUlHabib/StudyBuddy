package org.example.studybuddy.database;

import org.example.studybuddy.model.Room;
import org.example.studybuddy.model.RoomParticipant;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomDAO {
    private Connection connection;

    public RoomDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Create a new room
    public Room createRoom(String name, String description, int createdBy, int maxParticipants) {
        String roomCode = generateRoomCode();
        String sql = "INSERT INTO rooms (name, description, room_code, created_by, max_participants) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, roomCode);
            stmt.setInt(4, createdBy);
            stmt.setInt(5, maxParticipants);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int roomId = generatedKeys.getInt(1);

                        // Add creator as admin
                        addParticipant(roomId, createdBy, "admin");

                        Room room = new Room(name, description, roomCode, createdBy, maxParticipants);
                        room.setId(roomId);
                        return room;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Generate unique room code
    private String generateRoomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        // Generate 6-character code
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        // Check if code exists, regenerate if it does
        if (roomCodeExists(code.toString())) {
            return generateRoomCode(); // Recursive call
        }

        return code.toString();
    }

    private boolean roomCodeExists(String roomCode) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, roomCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Join room by code
    public boolean joinRoom(String roomCode, int userId) {
        Room room = getRoomByCode(roomCode);
        if (room == null || !room.isActive() || room.isFull()) {
            return false;
        }

        return addParticipant(room.getId(), userId, "member");
    }

    // Add participant to room
    private boolean addParticipant(int roomId, int userId, String role) {
        String sql = "INSERT OR REPLACE INTO room_participants (room_id, user_id, role) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            stmt.setString(3, role);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get room by code
    public Room getRoomByCode(String roomCode) {
        String sql = "SELECT * FROM rooms WHERE room_code = ? AND is_active = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, roomCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Room room = new Room();
                    room.setId(rs.getInt("id"));
                    room.setName(rs.getString("name"));
                    room.setDescription(rs.getString("description"));
                    room.setRoomCode(rs.getString("room_code"));
                    room.setCreatedBy(rs.getInt("created_by"));
                    room.setMaxParticipants(rs.getInt("max_participants"));
                    room.setActive(rs.getBoolean("is_active"));
                    room.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    room.setParticipantCount(getParticipantCount(room.getId()));

                    return room;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get rooms user is part of
    public List<Room> getUserRooms(int userId) {
        List<Room> rooms = new ArrayList<>();
        String sql = """
            SELECT r.*, COUNT(rp.user_id) as participant_count 
            FROM rooms r 
            JOIN room_participants rp ON r.id = rp.room_id 
            WHERE rp.user_id = ? AND rp.is_active = 1 AND r.is_active = 1
            GROUP BY r.id 
            ORDER BY r.created_at DESC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room();
                    room.setId(rs.getInt("id"));
                    room.setName(rs.getString("name"));
                    room.setDescription(rs.getString("description"));
                    room.setRoomCode(rs.getString("room_code"));
                    room.setCreatedBy(rs.getInt("created_by"));
                    room.setMaxParticipants(rs.getInt("max_participants"));
                    room.setActive(rs.getBoolean("is_active"));
                    room.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    room.setParticipantCount(rs.getInt("participant_count"));

                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    // Get room participants - Fixed method name
    public List<RoomParticipant> getRoomParticipants(int roomId) {
        List<RoomParticipant> participants = new ArrayList<>();
        String sql = """
            SELECT rp.*, u.username 
            FROM room_participants rp 
            JOIN users u ON rp.user_id = u.id 
            WHERE rp.room_id = ? AND rp.is_active = 1 
            ORDER BY rp.role, rp.joined_at
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RoomParticipant participant = new RoomParticipant();
                    participant.setId(rs.getInt("id"));
                    participant.setRoomId(rs.getInt("room_id"));
                    participant.setUserId(rs.getInt("user_id"));
                    participant.setUsername(rs.getString("username"));
                    participant.setRole(rs.getString("role"));
                    participant.setJoinedAt(rs.getTimestamp("joined_at").toLocalDateTime());
                    participant.setActive(rs.getBoolean("is_active"));

                    participants.add(participant);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participants;
    }

    // Get participant count
    private int getParticipantCount(int roomId) {
        String sql = "SELECT COUNT(*) FROM room_participants WHERE room_id = ? AND is_active = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Leave room
    public boolean leaveRoom(int roomId, int userId) {
        String sql = "UPDATE room_participants SET is_active = 0 WHERE room_id = ? AND user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if user is in room
    public boolean isUserInRoom(int roomId, int userId) {
        String sql = "SELECT COUNT(*) FROM room_participants WHERE room_id = ? AND user_id = ? AND is_active = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Get user role in room
    public String getUserRoleInRoom(int roomId, int userId) {
        String sql = "SELECT role FROM room_participants WHERE room_id = ? AND user_id = ? AND is_active = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Delete room (admin only)
    public boolean deleteRoom(int roomId, int userId) {
        // Check if user is admin
        String checkRole = "SELECT role FROM room_participants WHERE room_id = ? AND user_id = ? AND is_active = 1";

        try (PreparedStatement stmt = connection.prepareStatement(checkRole)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && "admin".equals(rs.getString("role"))) {
                    // User is admin, proceed with deletion
                    String deleteRoom = "UPDATE rooms SET is_active = 0 WHERE id = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteRoom)) {
                        deleteStmt.setInt(1, roomId);
                        return deleteStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
