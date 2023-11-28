

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CommonConfigHelper {
    private int numPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;

    public CommonConfigHelper(String filePath) {
        this.processFile(filePath);
    }

    private void processFile(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();

            // Loop through each line in file
            while (line != null) {
                String[] splitLine = line.split(" ");
                switch (splitLine[0]) {
                    case "NumberOfPreferredNeighbors":
                        this.numPreferredNeighbors = Integer.parseInt(splitLine[1]);
                        break;
                    case "UnchokingInterval":
                        this.unchokingInterval = Integer.parseInt(splitLine[1]);
                        break;
                    case "OptimisticUnchokingInterval":
                        this.optimisticUnchokingInterval = Integer.parseInt(splitLine[1]);
                        break;
                    case "FileName":
                        this.fileName = splitLine[1];
                        break;
                    case "FileSize":
                        this.fileSize = Integer.parseInt(splitLine[1]);
                        break;
                    case "PieceSize":
                        this.pieceSize = Integer.parseInt(splitLine[1]);
                        break;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public int getNumPreferredNeighbors() {
        return this.numPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return this.unchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return this.optimisticUnchokingInterval;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getFileSize() {
        return this.fileSize;
    }

    public int getPieceSize() {
        return this.pieceSize;
    }
}
