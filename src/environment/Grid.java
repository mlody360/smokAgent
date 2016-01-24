package environment;

import java.io.Serializable;



public class Grid implements Serializable {
	
	public  int nLines = 5;
	private int nbPits = 4;
	private Cell [][] matrix = new Cell [nLines][nLines];
	private int [][] pitPositionsMatrix = new int [nbPits][2];
	private int hunterPositionX, hunterPositionY;
	
	
	/**
	 * Creates a 2-D array of Cell objects
	 * And fills it
	 */
	public Grid() { 
		for (int i = 0; i < nLines; i++) {
			for ( int j = 0; j < nLines; j++) {
				matrix[i][j] = new Cell(i,j);
			}
		}
	}
	
	/*
	public int generateRandom() {
		//generates a random number between 0 and 9
		int random = (int)(10.0 * Math.random());
		//if random = 8 or 9 divide by 2 (columns indexes form 0 to 7)
		if (random > 7) {
			random = (int)(random / 2);		
		}
	return	random;
	}*/

	public void setGridCellContent() {
		int [][] pitPosMatrix;
		for (int i = 0; i < nLines; i++) {
			for(int j = 0; j < nLines; j++){
				matrix[i][j].resetBooleanObjects();
			}
		}
		setHunterPosition(nLines - 1, 0);
		//setWumpusAndStench(generateRandom(), generateRandom());
		//setGold(generateRandom(), generateRandom());
		setWumpusAndStench(2,0);
		setGold(2, 1);
		pitPosMatrix = generatePitsPositions(nbPits);
		for(int k = 0; k < nbPits; k++) {
			setPitAndBreeze(pitPosMatrix[k][0], pitPosMatrix[k][1]);
		}
	}
	
	public void setHunterPosition(int i, int j) {
		matrix[i][j].hunter = true;
		hunterPositionX = i;
		hunterPositionY = j;
	}
	
	/**
	 * Generates the coordinates (x,y) of pits
	 * and returns the matrix of all pits positions
	 */
	public int[][] generatePitsPositions(int nbPits) {
		/*
		for (int i = 0; i < nbPits; i++) {
			pitPositionsMatrix[i][0] = generateRandom();
			pitPositionsMatrix[i][1] = generateRandom();
		}*/
		
		pitPositionsMatrix[0][0] = 1; pitPositionsMatrix[0][1] = 2; 
		pitPositionsMatrix[1][0] = 4; pitPositionsMatrix[1][1] = 4;
		pitPositionsMatrix[2][0] = 0; pitPositionsMatrix[2][1] = 4;
		pitPositionsMatrix[3][0] = 3; pitPositionsMatrix[3][1] = 1;
		
		return pitPositionsMatrix;
	}

	public void setPitAndBreeze(int i, int j) { 
		if (!matrix[i][j].hunter) {
			matrix[i][j].pit = true;
		}
		if (i-1 >= 0) {
			if (matrix[i-1][j].pit == false) {
				matrix[i-1][j].breeze = true;
			}
		}
		if (i+1 <= nLines - 1) {
			if (matrix[i+1][j].pit == false) {
				matrix[i+1][j].breeze = true;
			}
		}
		if (j-1 >= 0) {
			if (matrix[i][j-1].pit == false) {
				matrix[i][j-1].breeze = true;
			}
		}
		if (j+1 <= nLines - 1) {
			if (matrix[i][j+1].pit == false) {
				matrix[i][j+1].breeze = true;
			}
		}
		
	}
		
	public void setWumpusAndStench(int i, int j) {
		if (!matrix[i][j].hunter && !matrix[i][j].pit) {
			matrix[i][j].wumpus = true;	
		}
		if (i-1 >= 0) {
			matrix[i-1][j].stench = true;
		}
		if (i+1 <= nLines - 1) {
			matrix[i+1][j].stench = true;
		}
		if (j-1 >= 0) {
			matrix[i][j-1].stench = true;
		}
		if (j+1 <= nLines - 1) {
			matrix[i][j+1].stench = true;
		}
	}

	public void setGold(int i, int j) {
		if (!matrix[i][j].hunter && !matrix[i][j].wumpus && !matrix[i][j].pit) {
			matrix[i][j].gold = true;
		}
	}
	
	public int getHunterPosX() {
		return this.hunterPositionX;
	}
	
	public int getHunterPosY() {
		return this.hunterPositionY;
	}
		
	/**
	 * returns the cell in which the hunter is standing
	 */
	public Cell getHunterCell() {
		return matrix[hunterPositionX][hunterPositionY];
	}
	
	/**
	 * returns the cell that has (i,j) coordinates
	 */
	public Cell getCell(int i, int j) {
		return matrix[i][j];
	}
	
}
