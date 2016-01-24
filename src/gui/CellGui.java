package gui;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import environment.Cell;



public class CellGui extends Canvas {
	
	private static final long serialVersionUID = 1L;
	private int width, height;
	private Cell c;
	public boolean wumpusIsKilled = false;
	public boolean hunterIsKilled = false;
	public boolean hunterWins     = false;
	public boolean disableStench  = false;
	
	
	/**
	 *Initializes the width and height of a cellGui
	 */
	public CellGui(int width, int height, Cell c) {
		super();
		this.width = width;
		this.height = height;
		this.c  = c;
	}
	
	/**
	 * Overrides the paint method of the Canvas class
	 */	
	@Override
	public void paint(Graphics g) {
		g.setColor(Color.black);
		g.drawRect(0, 0, width, height);
		drawCellGuiContent(g);
		DisplayKilledOrWin(g);
	}
	
	/**
	 * Draws a string/drawring in the Gui's cell that matches the object existing in the Matrix' cell
	 */
	public void drawCellGuiContent(Graphics g) {
		
		if (c.hunter) {
			g.setColor(Color.GREEN);
			Font big = new Font("SansSerif", Font.BOLD, 16);
			g.setFont(big);
			g.drawString("Hunter", 15, 20);		
		} 
		
		if (c.wumpus) {
			g.setColor(Color.red);
			g.fillOval(15, 15, 50, 50);
			g.setColor(Color.black);
			g.fillOval(25, 25, 10, 10);
			g.setColor(Color.black);
			g.fillOval(45, 25, 10, 10);
			g.setColor(Color.black);
			g.fillRect(33, 45, 15, 10);
		} 
		
		if (c.breeze) {
			g.setColor(Color.magenta);
			Font big = new Font("SansSerif", Font.BOLD, 14);
			g.setFont(big);
			g.drawString("))))))))", 15, 45);
		} 
		
		if (c.pit) {
			g.setColor(Color.black);
			g.fillOval(15, 15, 50, 50);
		}
		
		if (c.stench) {
			g.setColor(Color.blue);
			Font big = new Font("SansSerif", Font.BOLD, 14);
			g.setFont(big);
			g.drawString("sssss", 15, 60);
		}
		
		if (c.gold) {
			g.setColor(Color.yellow);
			g.fillRect(15, 65, 40, 15);
		}	
	}
	
	/**
	 * Displays the final status of hunter and wumpus
	 * Changes the wumpus color to gray
	 * Displays WIN
	 * @param g
	 */
	public void DisplayKilledOrWin(Graphics g){
		
		// Displays Hunter in a new color when killed
		if (this.hunterIsKilled) { 
			g.setColor(Color.gray);
			Font big = new Font("SansSerif", Font.BOLD, 16);
			g.setFont(big);
			g.drawString("Hunter", 15, 20);	
		} 
		
		// Displays WIN when hunter takes the gold
		if (this.hunterWins) { 
			g.setColor(Color.white);
			Font big1 = new Font("SansSerif", Font.BOLD, 16);
			g.setFont(big1);
			g.drawString("Hunter", 15, 20);	
			g.setColor(Color.RED);
			Font big = new Font("SansSerif", Font.BOLD, 18);
			g.setFont(big);
			g.drawString("W I N", 15, 20);	
		}
		
		// Displays Wumpus in a new color when killed
		if (this.wumpusIsKilled) { 
			g.setColor(Color.gray);
			g.fillOval(15, 15, 50, 50);
			g.setColor(Color.black);
			g.fillOval(25, 25, 10, 10);
			g.setColor(Color.black);
			g.fillOval(45, 25, 10, 10);
			g.setColor(Color.black);
			g.fillRect(33, 45, 15, 10);
		}
		
		// Disables stench - turns them to gray when Wumpus' killed
		if (this.disableStench) {
			g.setColor(Color.gray);
			Font big = new Font("SansSerif", Font.BOLD, 14);
			g.setFont(big);
			g.drawString("sssss", 15, 60);
		}
	}
	
	public Cell getCell() {
		return this.c;
	}
	
	public void updateCellGui () {
		this.repaint();
	}

}
