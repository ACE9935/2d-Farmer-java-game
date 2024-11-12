package fr.ubx.poo.ugarden;

import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.launcher.MapEntity;
import fr.ubx.poo.ugarden.launcher.MapLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Utils {
    // Private constructor to prevent instantiation of this utility class
    private void ArrayUtils() {
        throw new AssertionError(); // throw an error if someone tries to instantiate this class
    }

    public static <T> T[] filterArrayByClass(Object[] array, Class<T> clazz) {
        List<T> filteredList = new ArrayList<>();
        for (Object obj : array) {
            if (clazz.isInstance(obj)) {
                filteredList.add(clazz.cast(obj));
            }
        }
        // Convert the list to an array of the desired type
        return filteredList.toArray((T[]) java.lang.reflect.Array.newInstance(clazz, filteredList.size()));
    }
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            randomString.append(randomChar);
        }

        return randomString.toString();
    }
    public static List<Position> generateRandomPositions(int count,int width, int height, Position playerPosition,int level) {
        List<Position> positions = new ArrayList<>();
        Random random = new Random();
        int minDistance = 3; // Minimum distance from the player position

        for (int i = 0; i < count; i++) {
            Position newPosition;
            do {
                // Generate random x and y coordinates
                int x = random.nextInt(width); // Adjust the maximum value according to your requirement
                int y = random.nextInt(height); // Adjust the maximum value according to your requirement

                // Create a new position
                newPosition = new Position(level, x, y);
            } while (distance(playerPosition, newPosition) < minDistance);

            // Add the new position to the list
            positions.add(newPosition);
        }

        return positions;
    }
    public static double distance(Position p1, Position p2) {
        return Math.sqrt(Math.pow(p2.x() - p1.x(), 2) + Math.pow(p2.y() - p1.y(), 2));
    }

    public static List<MapLevel> loadMaps(File file) throws IOException {
        List<MapLevel> allLevels = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            boolean compression = false; // Default value for compression
            int numLevels = 0;
            int level=1;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("compression")) {
                    compression = Boolean.parseBoolean(line.split("=")[1].trim());
                } else if (line.startsWith("levels")) {
                    numLevels = Integer.parseInt(line.split("=")[1].trim());
                } else if (line.startsWith("level")) {
                    String layout = line.split("=")[1].trim(); // Get layout from the same line
                    MapEntity[][] map = parseLayout(layout, compression);
                    MapLevel mapLevel=new MapLevel(map[0].length,map.length,level);
                    level++;
                    mapLevel.setGrid(map);
                    allLevels.add(mapLevel);
                    if (allLevels.size() == numLevels) {
                        // Stop reading levels if we've reached the specified number
                        break;
                    }
                }
            }
        }
        return allLevels;
    }

    private static MapEntity[][] parseLayout(String layout, boolean compression) {
        List<String> rows = new ArrayList<>();
        // Split the layout by 'x' and handle compression
        if (compression) {
            StringBuilder rowBuilder = new StringBuilder();
            for (int i = 0; i < layout.length(); i++) {
                char c = layout.charAt(i);
                if (Character.isDigit(c)) {
                    // If the character is a digit, repeat the last character 'n' times
                    char lastChar = rowBuilder.charAt(rowBuilder.length() - 1);
                    int repeatCount = Character.getNumericValue(c);
                    for (int j = 0; j < repeatCount - 1; j++) {
                        rowBuilder.append(lastChar);
                    }
                } else if (c != 'x') {
                    rowBuilder.append(c);
                } else {
                    rows.add(rowBuilder.toString());
                    rowBuilder.setLength(0); // Reset the StringBuilder
                }
            }
            // Add the last row
            if (rowBuilder.length() > 0) {
                rows.add(rowBuilder.toString());
            }
        } else {
            rows = Arrays.asList(layout.split("x"));
        }

        // Debugging output
        System.out.println("Rows: " + rows);
        System.out.println("NumRows: " + rows.size());
        System.out.println("First row length: " + (rows.isEmpty() ? 0 : rows.get(0).length()));

        int numRows = rows.size();
        int numCols = numRows > 0 ? rows.get(0).length() : 0;
        MapEntity[][] map = new MapEntity[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            String row = rows.get(i);
            for (int j = 0; j < row.length(); j++) {
                char c = row.charAt(j);
                map[i][j] = MapEntity.fromCode(c);
            }
        }
        return map;
    }
}