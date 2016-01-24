package gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;

import environment.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



public class GridGui extends Frame {

	private Grid grid;
	private int nLines = 5;
	
	/**
	 * Initializes the frame and draws the grid
	 */
	public GridGui(Grid g) {
		super();
		this.grid = g;
	}
	
	
	public void showGui() {
		drawGridGui();
		this.setSize(420, 440);
		this.setLayout(new GridLayout(nLines,nLines));
		this.setBackground(Color.white);
		this.setResizable(false);
		this.setVisible(true);
                this.addWindowListener ( new WindowAdapter () {
		public void windowClosing ( WindowEvent evt )
                    {
                            System.exit(0);
                    }
                });
	}
	
	
	/**
	 * Draws an nxn grid by creating a cellGui object and adding it to the frame.
	 */
	public void drawGridGui() { 
		for (int i = 0; i < nLines; i++) {
			for (int j = 0; j < nLines; j++) {
				this.add(new CellGui(100,100, grid.getCell(i, j)));
			}
		}
	}
	
	/**
	 * 
	 */
	public CellGui getCellGui(int x, int y){
		
		for(int i = 0; i < this.getComponents().length; i++){
			CellGui c = (CellGui)this.getComponent(i);
				if (c.getCell().getX() == x && c.getCell().getY() == y) {
					return c;
				}
		} 
		return null;
	}
	
	
}