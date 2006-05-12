import java.awt.* ;
import java.awt.event.* ;
import java.awt.Graphics2D;
import java.awt.image. BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.io.File;

class Player_Info
{
	String username;
}

class Tile_Info
{
	String tilename;
	BufferedImage img_file;

	Tile_Info ( String tilename, BufferedImage img_file)
	{
		this.tilename = tilename;
		this.img_file = img_file;
	}
}

class Instance_Info
{
	int TI_ref;		// the index for the Tile_Info
	int owner;		// the owner of the tile (0 is neutral)

	Instance_Info ( int TI_ref, int owner )
	{
		this.TI_ref = TI_ref;
		this.owner = owner;
	}
}

public class ECONGAME extends Panel implements ActionListener, MouseListener
{
	int 	lot_cols = 10;		// number of columns of lots
	int 	lot_rows = 10;		// number of rows of lots
	int 	lot_size = 20;		// the size of the lots
	int 	gameboard_x = 50;	// the location of the left of the game board
	int 	gameboard_y = 50;	// the location of the top of the game board
	int 	col_hover = 0;		// column that is being hovered over
	int		row_hover = 0;		// row that is being hovered over

	// constants to make referring to stuff easier
	final int LOT_UNEXPLORED	= 0;

	// the list of all the tiles and their unique properties
	Tile_Info[]			tile_list = new Tile_Info[]
	{
		new Tile_Info ( "Unexplored Lot", ImageIO.read(new File( "lot_unexplored.gif" )) ),	// LOT_UNEXPLORED - 0
	};

	// the list of all the instances of lots on the board
	Instance_Info[][]	instance_list = new Instance_Info[lot_cols][lot_rows];

	ECONGAME() throws IOException
	{
		// initialize all the lots on the map with an undiscovered lot
		for ( int col = 0; col < lot_cols; col++ )
		{
			for ( int row = 0; row < lot_rows; row++ )
			{
				instance_list[col][row] = new Instance_Info( LOT_UNEXPLORED, 0 );
			}
		}

		addMouseListener(this);
	}

	public void actionPerformed( ActionEvent ae )
	{
		// ae.getSource() ;
	} // end of actionPerformed

	// Mouse Events

	public void mouseClicked ( MouseEvent ke )
	{
		int mousex, mousey;
		if ( ke.getButton() == 3 )		// when right mouse button is clicked
		{
			Dimension d = this.getSize();
			mousex = ke.getX();			// save coordinates
			mousey = ke.getY();
			// if the click occured when the cursor was inside the gameboard parameters
			if (   (mousex >= gameboard_x) && (mousex <= gameboard_x+(lot_size+1)*lot_cols-1)
				&& (mousey >= gameboard_y) && (mousey <= gameboard_y+(lot_size+1)*lot_rows-1) )
			{
				// extract the column/row
				col_hover = (int) Math.floor((mousex-gameboard_x)/(lot_size+1));
				row_hover = (int) Math.floor((mousey-gameboard_y)/(lot_size+1));
				System.out.println(col_hover+" "+row_hover);
			}
		}
	}

	public void mousePressed ( MouseEvent ke )
	{
	}

	public void mouseReleased ( MouseEvent ke )
	{
	}

	public void mouseEntered ( MouseEvent ke )
	{
	}

	public void mouseExited ( MouseEvent ke )
	{
	}

	public void paint( Graphics g )
	{
		Dimension d = this.getSize();	// get the dimension of the window
		int w = d.width;
		int h = d.height;

		// text stuff
		g.setFont( new Font("SansSerif", Font.BOLD, 12) ) ;
		// game board backing
		g.setColor(Color.BLACK);
		g.fillRect ( gameboard_x-2, gameboard_y-2, (lot_size+1)*lot_cols+3, (lot_size+1)*lot_rows+3);

		// draw the individual lots
		for ( int col = 0; col < lot_cols; col++ )
		{
			for ( int row = 0; row < lot_rows; row++ )
			{
				g.drawImage ( 	tile_list[ instance_list[col][row].TI_ref ].img_file,
								gameboard_x+(lot_size+1)*col,
								gameboard_y+(lot_size+1)*row,
								lot_size, lot_size, this );
			}
		}
	};

	public int roll_die ()		// dice rolling simulation
	{
		return ((int) Math.round(Math.random()*6 + 0.5));
	}
}