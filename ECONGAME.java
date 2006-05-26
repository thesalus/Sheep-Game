import java.awt.* ;
import java.awt.event.* ;
import java.applet.* ;
import java.awt.Graphics;
import java.awt.Image;
import java.net.*;
import java.util.*;

class Player_Info
{
	String	username;
	Color	usercolour;
	int[]	resources = new int[4];

	Player_Info ( String username, Color usercolour )
	{
		this.username = username;
		this.usercolour = usercolour;

		for ( int i = 0; i < 3; i++ )
		{
			resources[i] = (((int) Math.round(Math.random()*2 + 0.5))+1)*100;
		}
		resources[3] = 1000-(resources[0]+resources[1]+resources[2]);
	}
}

class Tile_Info
{
	String tilename;
	Image img_file;

	Tile_Info ( String tilename, Image img_file)
	{
		this.tilename = tilename;
		this.img_file = img_file;
	}
}

class Instance_Info
{
	int TI_ref;		// the index for the Tile_Info
	int owner;		// the owner of the tile (0 is neutral)
	int sheep;		// the number of sheep on the tile (up to 4) 5 is the factory-ish

	Instance_Info ( int TI_ref, int owner, int sheep )
	{
		this.TI_ref = TI_ref;
		this.owner = owner;
		this.sheep = sheep;
	}
}

class Card_Info
{
	String cardname;	// the name of the card
	String explanation;	// the text on the card
	int removed;		// number of turns until it is removed, 1 is removed at the beginning fo each turn
						// and those at 0 are removed;
	double rsrc1, rsrc2,rsrc3,rsrc4;	// the effect it has on the resources [multipliers]
	int exp_add, pop_add, ind_add, glo_add;	// the effect it has on these things
	int played;			// the number of instances left in deck
	int affected;		// who is affected? 0 - all; 1-notself
	int effect;			// the unique effect it calls. 0 - no effect

	Card_Info ( String cardname, String explanation, int removed, double rsrc1, double rsrc2, double rsrc3, double rsrc4, int exp_add, int pop_add, int ind_add, int glo_add, int played, int affected, int effect )
	{
		this.cardname = cardname;
		this.explanation = explanation;
		this.removed = removed;
		this.rsrc1 = rsrc1;
		this.rsrc2 = rsrc2;
		this.rsrc3 = rsrc3;
		this.rsrc4 = rsrc4;
		this.exp_add = exp_add;
		this.pop_add = pop_add;
		this.ind_add = ind_add;
		this.glo_add = glo_add;
		this.played = played;
		this.affected = affected;
		this.effect = effect;
	}

	// maybe seperate constructor for unique effects?
}

class Effect_Info
{
	int removed;
	double rsrc1, rsrc2,rsrc3,rsrc4;	// the effect it has on the resources [multipliers]
	int exp_add, pop_add, ind_add, glo_add;	// the effect it has on these things
	int affected;		// who is affected? 0 - all; 1-notself
	int effect;			// the unique effect it calls. 0 - no effect

	Effect_Info ( int removed, double rsrc1, double rsrc2, double rsrc3, double rsrc4, int exp_add, int pop_add, int ind_add, int glo_add, int affected, int effect )
	{
		this.removed = removed;
		this.rsrc1 = rsrc1;
		this.rsrc2 = rsrc2;
		this.rsrc3 = rsrc3;
		this.rsrc4 = rsrc4;
		this.exp_add = exp_add;
		this.pop_add = pop_add;
		this.ind_add = ind_add;
		this.glo_add = glo_add;
		this.affected = affected;
		this.effect = effect;
	}
}

public class ECONGAME extends Applet implements MouseListener
{
	// Game Constants
	int		numofplayers = 5;	// number of players
	int 	lot_cols = 10;		// number of columns of lots
	int 	lot_rows = 10;		// number of rows of lots
	int 	lot_size = 30;		// the size of the lots
	int		lot_space = 2;		// space between the lots
	int 	gameboard_x = 50;	// the location of the left of the game board
	int 	gameboard_y = 50;	// the location of the top of the game board
	URL 	base;

	int 	col_hover = 0;		// column that is being hovered over
	int		row_hover = 0;		// row that is being hovered over
	int		disp_col = 0;		// the column and row of the thing being displayed
	int 	disp_row = 0;

	// Turn's Variables
	int		cur_turn = 0;		// whose turn is it?
	int		tilesexplored = 5;	// how many tiles have been explored?
	int		numactions;			// number of actions performed so far
	int 	rsrc1_add, rsrc2_add, rsrc3_add, rsrc4_add;
			// number of each resource added
	int		explore_num, populate_num, randomize_num, globalize_num;
			// number of times that thing can be done a turn.
	int		tradepartner = 0;	// the trading partner

	int 	active_effect = 1;	// the current effect active
	String 	effectmsg = "";		// message

	// Card Variables
	int 	cardnum;			// the currently selected card's number
	boolean displaycard;		// flag for whether or not to display card
	int		cardspicked;		// number of cards picked up
	int		decksize = 50; 		// number of cards in the deck

	// Temporary Variables
	int		tempcol=0,temprow=0;// the temporary row and column storing things
	int		temp = 0;			// multi-use temp variable

	int[] 		trade = {0, 0, 0, 0};
	double		interestrate = 3;			// interest rate in percent
	int[]		exchange = { 100, 100, 100, 100 };	// the exchange rate
	int[]		loan = {2000, 2000, 2000, 2000, 2000};	// initial loan
	int[] 		paying = {0,0,0,0};			// amount current player is paying

	// constants to make referring to stuff easier
	final int LOT_UNEXPLORED	= 0;
	final int LOT_FOREST		= 2;

	final int UNEXPLORED_BIG 	= 1;
	final int FOREST_BIG		= 3;

	final int SHEEP_IMG			= 16;
	final int BG_IMG			= 10;
	final int RESOURCE_IMG		= 17;
	final int INDUSTRY_IMG		= 20;
	final int CARD_IMG			= 31;

	final int CARD_LNGTH		= 25;

	// resource names
	String[]	resource_names =
	{
		"Very Green Grass",		// 0
		"Anthonium Ore",		// 1
		"Grub",					// 2
		"Tender Love and Care"	// 3
	};

	// amount of resource added each turn
	int[] 		rsrc_amt = { 100, 100, 100, 100 };

	// list of players and their associate colours
	Player_Info[]		player_list = new Player_Info[]
	{
		new Player_Info ( "Neutral", Color.BLACK ),
		new Player_Info ( "Player 1", Color.RED ),
		new Player_Info ( "Player 2", Color.MAGENTA ),
		new Player_Info ( "Player 3", Color.GREEN ),
		new Player_Info ( "Player 4", Color.YELLOW ),
		new Player_Info ( "Player 5", Color.CYAN)
	};

	// the list of all the instances of lots on the board
	Instance_Info[][]	instance_list = new Instance_Info[lot_cols][lot_rows];

	Tile_Info[] tile_list;

	Card_Info[] 		card_list = new Card_Info[]
	{
		new Card_Info ( "Supersheep", "Exploration may be \nperformed twice.",
						1, 1, 1, 1, 1, 1, 0, 0, 0, 3, 0, 0 ),
		new Card_Info ( "Sheeptastic!", "Population may be \nperformed twice.",
						1, 1, 1, 1, 1, 0, 1, 0, 0, 3, 0, 0 ),
		new Card_Info ( "Silence of the Sheep", "Do not collect resources \nthis turn. Do not herd \nyour sheep.",
						1, 0, 0, 0, 0, -1, -1, -1, -1, 2, 0, 0 ),
		new Card_Info ( "Sheep Without Borders", "Globalization may be \nperformed with non-\nadjacent players.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 3, 0, 1 ),		// need seperate effect for this
		new Card_Info ( "Invasion of the Sheep", "Take control of a tile \nbelonging to another \nplayer that is adjacent \nto a tile you already \ncontrol.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 0, 2 ),		// need seperate effect for this
		new Card_Info ( "Don't Give Me That Sheep", "No other players collect \nresources until your next \nturn.",
						numofplayers, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0 ),
		new Card_Info ( "I Need to Go Take a Sheep", "Take another player’s \nsheep and place it on a \ntile you control.  The \nsheep taken cannot be \nthe only sheep on a tile \nand cannot be on a capital \ntile.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 0, 3 ),		// seperate effect for this
		new Card_Info ( "Mad Sheep Disease", "The Japanese decide \nto close their borders \ndue to Ovine Spongiform \nEncephalopathy, so \ndemand for sheep \ndecreases. Each piece of \nland contributes 25% less \nresource points for a bit.",
						2*numofplayers, 0.75, 0.75, 0.75, 0.75, 0, 0, 0, 0, 1, 0, 0 ),
		new Card_Info ( "Sheep Mania", "Mattel Inc releases a \nnew line of Barbie Sheep, \nincreasing the demand \nfor sheep in general. \nEach piece of land \ncontributes 10% \nresource points for \na bit.",
						2*numofplayers, 1.25, 1.25, 1.25, 1.25, 0, 0, 0, 0, 1, 0, 0 ),
		new Card_Info ( "Microsoftsheep Inc.", "Microsoftsheep has \nbought up the \ncompeting sheep \nbuyers, creating a \nmonopsony. Each piece \nof land contributes \n10% less resource points \nfor a bit.",
						2*numofplayers, 0.75, 0.75, 0.75, 0.75, 0, 0, 0, 0, 1, 0, 0 ),
		new Card_Info ( "Bull Sheep", "Players may not trade \nuntil your next turn.",
						numofplayers, 1, 1, 1, 1, 0, 0, 0, -1, 3, 1, 0 ),
		new Card_Info ( "Sheep Market", "Name a resource type. \nIf no other player owns \na land of that resource \ntype, you possess a \nmonopoly. Each land \nyou possess of that type \ncontributes 10% more \nresource points for \na bit.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 4, 0, 4 ),
		new Card_Info ( "Black Sheep Friday", "As sheep stocks crash, \npeople pull money out \nof their banks to pay \ntheir debts. Interest \nrates increase by 5%.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 5 ),
		new Card_Info ( "Sheepen Harper's Fiscal Policy", "Sheepen Harper decides \nto decrease the income \ntaxes on the resources \nof the sheep. Each player \nreceives 10% more \nresource points for each \ntile for a while.",
						5*numofplayers, 1.1, 1.1, 1.1, 1.1, 0, 0, 0, 0, 2, 0, 0 ),
		new Card_Info ( "Xenophobic sheep", "Uncertainty in the US \ndollar has caused the \nvalue of the Canadian \ndollar to skyrocket. Thus, \nthe effective price of the \nsheep has increased, so \ndemand decreases. Each \nplayer receives 10% less \nresource points for a bit.",
						5*numofplayers, 0.9, 0.9, 0.9, 0.9, 0, 0, 0, 0, 2, 0, 0 ),
		new Card_Info ( "Sheep-Tippers", "They strike in the night \nleaving your sheep a tad \ntipsy. Each piece of land \ncontributes 20% less \nresource points for a bit.",
						2*numofplayers, 0.8, 0.8, 0.8, 0.8, 0, 0, 0, 0, 4, -1, 0 ),
		new Card_Info ( "Absurd? Insane?! Supersheep!", "A large ovinite meteor hits \none of your plots of land. \nThey gain superpowers \nand fly away. You lose all \nbut one sheep on that lot.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 0, 6 ),
		// so far... at card 17 with 38 cards
		new Card_Info ( "Herbal Magic Weight Loss", "The latest trend in weight \nloss programs has demand \nfor the Very Green Grass \nsoaring, and thus its \nvalue.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 7 ),
		new Card_Info ( "Fight Ore Flight", "Paperweights have made \na comeback! Demand for \nAnthonium Ore soars, \nand so does its value.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 8 ),
		new Card_Info ( "Grub for Money", "Two words. Grub Soup. \nDemand for Grub soars, \nas does its value.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 9 ),
		new Card_Info ( "I'm lovin' it", "Demand for smiles \nincreases, as does the \nvalue of Tender Love \nand Care.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 10 ),
		new Card_Info ( "The Other Side of the Hill", "Scientists have produced \nExtremely Green Grass, \ncausing demand for Very \nGreen Grass to plummet, \nand its value follows suit.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 0, 11 ),
		new Card_Info ( "Bust Ore Boom", "Orelando, a major importer \nof Anthonium Ore switches to \neco-friendly Ethanonium \nGas. Demand for Anthonium \nOre decreases, as does \nits value.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 0, 12 ),
		new Card_Info ( "Money for Grub", "Heavy tariffs have been \nimposed as to protect \nhomegrown grubs in many \nmajor importer countries. \nDemand for your Grub \ndrops, sending its value to \nthe bargain bin.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 0, 13 ),
		new Card_Info ( "She Loves Me Not", "Even-petalled flowers \ndominate the industry. \nDisheartened fellows have \nno room for Tender Love \nand Care. Values jump off \nthe bridge.",
						1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 0, 14 ),
	};

	LinkedList 			effect_list = new LinkedList();

 	public void init()
 	{
		try
		{
			base = getCodeBase();
			// the list of all the tiles and their unique properties
		}
		catch (Exception e)
		{
			System.err.println("Couldn't read - error was " + e);
		}

		tile_list = new Tile_Info[]
		{
			new Tile_Info ( "Unexplored Lot", 	getImage(base, "images/lot_unexplored.gif" ) ),		// LOT_UNEXPLORED - 0
			new Tile_Info ( "Unexplored Big", 	getImage(base, "images/lot_unexplored_big.gif" ) ),	// UNEXPLORED BIG - 1
			new Tile_Info ( "Very Green Grass", getImage(base, "images/lot_forest.gif" ) ),			// LOT_FOREST - 2
			new Tile_Info ( "Forest Big", 		getImage(base, "images/lot_forest_big.gif" ) ),		// FOREST BIG - 3
			new Tile_Info ( "Anthonium Ore", 	getImage(base, "images/lot_mine.gif" ) ),			// LOT_GRUB - 4
			new Tile_Info ( "Mine Big", 		getImage(base, "images/lot_mine_big.gif" ) ),		// GRUB BIG - 5
			new Tile_Info ( "Grub Soil", 		getImage(base, "images/lot_grub.gif" ) ),			// LOT_MINE - 6
			new Tile_Info ( "Grub Big", 		getImage(base, "images/lot_grub_big.gif" ) ),		// MINE BIG - 7
			new Tile_Info ( "Petting Zoo", 		getImage(base, "images/lot_care.gif" ) ),			// LOT_CARE - 8
			new Tile_Info ( "Care Big", 		getImage(base, "images/lot_care_big.gif" ) ),		// CARE BIG - 9
			new Tile_Info ( "Neutral BG", 		getImage(base, "images/black_big.gif" ) ),			// P0 BG 10
			new Tile_Info ( "Player 1 BG", 		getImage(base, "images/red_big.gif" ) ),			// P1 BG
			new Tile_Info ( "Player 2 BG", 		getImage(base, "images/magenta_big.gif" ) ),		// P2 BG
			new Tile_Info ( "Player 3 BG", 		getImage(base, "images/green_big.gif" ) ),			// P3 BG
			new Tile_Info ( "Player 4 BG", 		getImage(base, "images/yellow_big.gif" ) ),			// P4 BG
			new Tile_Info ( "Player 5 BG", 		getImage(base, "images/blue_big.gif" ) ),			// P5 BG
			new Tile_Info ( "Sheep", 			getImage(base, "images/sheep.gif" ) ),				// Sheep 16
			new Tile_Info ( "Grass Resource", 	getImage(base, "images/resource_grass.gif" ) ),		// Grass 17
			new Tile_Info ( "Ore Resource", 	getImage(base, "images/resource_ore.gif" ) ),		// Grub
			new Tile_Info ( "Grub Resource", 	getImage(base, "images/resource_grub.gif" ) ),		// Ore
			new Tile_Info ( "Love Resource", 	getImage(base, "images/resource_love.gif" ) ),		// Love
			new Tile_Info ( "Very Green Grass Mines",	getImage(base, "images/lot_forest_ind.gif" ) ),		// Ind Forest 21
			new Tile_Info ( "Anthonium Ore Farms",		getImage(base, "images/lot_mine_ind.gif" ) ),		// Ind Grub
			new Tile_Info ( "Grub Replicators",			getImage(base, "images/lot_grub_ind.gif" ) ),		// Ind Mine
			new Tile_Info ( "Sheep Online Dating",		getImage(base, "images/lot_care_ind.gif" ) ),		// Ind Care
			new Tile_Info ( "Forest Bigbig",	getImage(base, "images/lot_forest_ind_big.gif" ) ),	// Ind Forest 25
			new Tile_Info ( "Grub Bigbig",		getImage(base, "images/lot_grub_ind_big.gif" ) ),	// Ind Grub
			new Tile_Info ( "Mine Bigbig",		getImage(base, "images/lot_mine_ind_big.gif" ) ),	// Ind Mine
			new Tile_Info ( "Care Bigbig",		getImage(base, "images/lot_care_ind_big.gif" ) ),	// Ind Care
			new Tile_Info ( "Trade",			getImage(base, "images/trade.gif" ) ),				// Trade - 29
			new Tile_Info ( "Pay Loan",			getImage(base, "images/payloan.gif" ) ),			// Pay Loan - 30
			new Tile_Info ( "Card 1",			getImage(base, "images/card3.gif" ) ),				// Card 1 - 31
			new Tile_Info ( "Card 2",			getImage(base, "images/card3.gif" ) ),				// Card 2
			new Tile_Info ( "Card 3",			getImage(base, "images/card3.gif" ) ),				// Card 3
			new Tile_Info ( "Card 4",			getImage(base, "images/card3.gif" ) ),				// Card 4
			new Tile_Info ( "Card 5",			getImage(base, "images/card5.gif" ) ),				// Card 5
			new Tile_Info ( "Card 6",			getImage(base, "images/card6.gif" ) ),				// Card 6
			new Tile_Info ( "Card 7",			getImage(base, "images/card7.gif" ) ),				// Card 7
			new Tile_Info ( "Card 8",			getImage(base, "images/card3.gif" ) ),				// Card 8
			new Tile_Info ( "Card 9",			getImage(base, "images/card3.gif" ) ),				// Card 9
			new Tile_Info ( "Card 10",			getImage(base, "images/card10.gif" ) ),				// Card 10
			new Tile_Info ( "Card 11",			getImage(base, "images/card3.gif" ) ),				// Card 11
			new Tile_Info ( "Card 12",			getImage(base, "images/card3.gif" ) ),				// Card 12
			new Tile_Info ( "Card 13",			getImage(base, "images/card3.gif" ) ),				// Card 13
			new Tile_Info ( "Card 14",			getImage(base, "images/card3.gif" ) ),				// Card 14
			new Tile_Info ( "Card 15",			getImage(base, "images/card3.gif" ) ),				// Card 15
			new Tile_Info ( "Card 16",			getImage(base, "images/card3.gif" ) ),				// Card 16
			new Tile_Info ( "Card 17",			getImage(base, "images/card17.gif" ) ),				// Card 17
			new Tile_Info ( "Card 18",			getImage(base, "images/card3.gif" ) ),				// Card 18
			new Tile_Info ( "Card 19",			getImage(base, "images/card3.gif" ) ),				// Card 19
			new Tile_Info ( "Card 20",			getImage(base, "images/card3.gif" ) ),				// Card 20
			new Tile_Info ( "Card 21",			getImage(base, "images/card3.gif" ) ),				// Card 21
			new Tile_Info ( "Card 22",			getImage(base, "images/card3.gif" ) ),				// Card 22
			new Tile_Info ( "Card 23",			getImage(base, "images/card3.gif" ) ),				// Card 23
			new Tile_Info ( "Card 24",			getImage(base, "images/card3.gif" ) ),				// Card 24
			new Tile_Info ( "Card 25",			getImage(base, "images/card3.gif" ) ),				// Card 25
		};

		int tempcol, temprow;
		// initialize all the lots on the map with an undiscovered lot
		for ( int col = 0; col < lot_cols; col++ )
		{
			for ( int row = 0; row < lot_rows; row++ )
			{
				instance_list[col][row] = new Instance_Info( LOT_UNEXPLORED, 0, 0 );
			}
		}

		// give all the players a starting position
		for ( int pnum = 1; pnum < 6; pnum++ )
		{
			tempcol = 2+roll_die(6);
			temprow = 2+roll_die(6);

			while ( instance_list[tempcol][temprow].owner != 0 )
			{
				tempcol = 2+roll_die(6);
				temprow = 2+roll_die(6);
			}

			instance_list[tempcol][temprow] = new Instance_Info ( (roll_die(4)+1)*2, pnum, 1);
		}

		change_turn();

		addMouseListener(this);
	}

	public void start()
	{
	}

	public void stop()
    {
    }

	// Mouse Events

	public void mouseClicked ( MouseEvent ke )
	{
		int mousex = ke.getX();			// save coordinates
		int mousey = ke.getY();

		// extract the column/row
		col_hover = (int) Math.floor((mousex-gameboard_x-lot_space+1)/(lot_size+lot_space));
		row_hover = (int) Math.floor((mousey-gameboard_y-lot_space+1)/(lot_size+lot_space));


		// if the click occured when the cursor was inside the gameboard parameters
		if (   (mousex >= gameboard_x-lot_space) && (mousex <= gameboard_x+(lot_size+lot_space)*lot_cols-1)
			&& (mousey >= gameboard_y-lot_space) && (mousey <= gameboard_y+(lot_size+lot_space)*lot_rows-1) )
		{
			if ( ke.getButton() == 1 )			// when left mouse button is clicked
			{
				// if the square is neutral
				if ( instance_list[col_hover][row_hover].owner == 0 )
				{
					// colour the square
					if ( (explore_num > 0) && check_adjacent ( col_hover, row_hover, cur_turn ) )
					{
						instance_list[col_hover][row_hover].owner = cur_turn;
						instance_list[col_hover][row_hover].TI_ref = (roll_die(4)+1)*2;
						instance_list[col_hover][row_hover].sheep = 1;
						repaint();			// repaint

						explore_num--;
						tilesexplored++;
						end_action();
					}
				}
			}
			else if ( ke.getButton() == 3 )		// when right mouse button is clicked
			{
				disp_col = col_hover;
				disp_row = row_hover;
				repaint();
			}
		}
		// [POP][IND] button
		else if ( (mousex >= 411) && (mousex <= 431) && (mousey >= 140) && (mousey <= 160 ) )
		{
			if ( ke.getButton() == 1 )			// when left mouse button is clicked
			{
				if ( dif_per() == false )
				{
					if ( populate_num > 0 && instance_list[disp_col][disp_row].sheep < 4 )
					{
						instance_list[disp_col][disp_row].sheep++;

						populate_num--;
						end_action();
					}
					else if ( populate_num > 0 && instance_list[disp_col][disp_row].sheep == 4 )
					{
						if ( player_list[cur_turn].resources[(instance_list[disp_col][disp_row].TI_ref/2)-1] > 500 )
						{
							instance_list[disp_col][disp_row].sheep++;
							player_list[cur_turn].resources[(instance_list[disp_col][disp_row].TI_ref/2)-1] -= 500;

							populate_num--;
							end_action();
						}
					}
					repaint();
				}
			}
		}
		// [RAN] button
		else if ( (mousex >= 363) && (mousex <= 436) && (mousey >= 465) && (mousey <= 487) )
		{
			if ( (cardspicked < decksize) && (randomize_num > 0) )
			{
				draw_card();
				apply_effects();
			}
		}
		// effects
		else if ( (mousex >= 240) && (mousex <= 340) && (mousey >= 465) && (mousey <= 487) )
		{
			if (active_effect == 2)			// if it is the second effect
			{
				if ( count_owned(instance_list[disp_col][disp_row].owner) > 1 )
				{
					if ( check_adjacent( disp_col, disp_row, cur_turn ) )
					{
						instance_list[disp_col][disp_row].owner = cur_turn;
						effectmsg = "";
						active_effect = 0;
					}
				}
			}
			else if ( active_effect == 3 )	// third effect A
			{
				if ( dif_per() )
				{
					if (instance_list[disp_col][disp_row].sheep > 1 && instance_list[disp_col][disp_row].sheep < 5)
					{
						active_effect = 50;
						tempcol = disp_col;
						temprow = disp_row;
					}
				}
			}
			else if (active_effect == 50)	// third effect B
			{
				if ( dif_per() == false )
				{
					if (instance_list[disp_col][disp_row].sheep < 4)
					{
						instance_list[tempcol][temprow].sheep--;
						instance_list[disp_col][disp_row].sheep++;
						effectmsg = "";
						active_effect = 0;
					}
				}
			}
			else if (active_effect == 4 )	// fourth effect
			{
				if ( dif_per() == false )
				{
					temp = 0;
					for ( int col = 0; col < lot_cols; col++ )
					{
						for ( int row = 0; row < lot_rows; row++ )
						{
							if ( instance_list[col][row].TI_ref == instance_list[disp_col][disp_row].TI_ref )
							{
								if ( instance_list[col][row].owner != cur_turn )
								{
									temp++;
								}
							}
						}
					}
					if ( temp == 0 )
					{
						effect_list.addLast ( new Effect_Info ( 81, ((instance_list[disp_col][disp_row].TI_ref/2)==1)?1.1:1,
																((instance_list[disp_col][disp_row].TI_ref/2)==2)?1.1:1,
																((instance_list[disp_col][disp_row].TI_ref/2)==3)?1.1:1,
																((instance_list[disp_col][disp_row].TI_ref/2)==4)?1.1:1,
																0, 0, 0, 0, 1+cur_turn, 0 ) );
					}
					else
					{
						effectmsg = "You do not have a monopoly.";
					}
					active_effect = 0;
				}
			}
			repaint();
		}
		// trade initiating button
		else if ( (mousex >= 505) && (mousex <= 526) && (mousey >= 60) && (mousey <= 81) )
		{
			if ( dif_per() && globalize_num>0)
			{
				tradepartner = instance_list[disp_col][disp_row].owner;
			}
			repaint();
		}
		// current player resource trade buttons
		else if ( (mousex >= 50) && (mousex <= 61) && (mousey >= 517) && (mousey <= 528) )
		{
			if ( player_list[cur_turn].resources[0] >= trade[0]+100 )
			{
				trade[0]+=100;
				repaint();
			}
		}
		else if ( (mousex >= 50) && (mousex <= 61) && (mousey >= 534) && (mousey <= 545) )
		{
			if ( player_list[cur_turn].resources[1] >= trade[1]+100 )
			{
				trade[1]+=100;
				repaint();
			}
		}
		else if ( (mousex >= 50) && (mousex <= 61) && (mousey >= 551) && (mousey <= 562) )
		{
			if ( player_list[cur_turn].resources[2] >= trade[2]+100 )
			{
				trade[2]+=100;
				repaint();
			}
		}
		else if ( (mousex >= 50) && (mousex <= 61) && (mousey >= 568) && (mousey <= 579) )
		{
			if ( player_list[cur_turn].resources[3] >= trade[3]+100 )
			{
				trade[3]+=100;
				repaint();
			}
		}
		// trading partner resource trade buttons
		else if ( (mousex >= 250) && (mousex <= 261) && (mousey >= 517) && (mousey <= 528) )
		{
			if ( player_list[tradepartner].resources[0] >= -trade[0]+100 )
			{
				trade[0]-=100;
				repaint();
			}
		}
		else if ( (mousex >= 250) && (mousex <= 261) && (mousey >= 534) && (mousey <= 545) )
		{
			if ( player_list[tradepartner].resources[1] >= -trade[1]+100 )
			{
				trade[1]-=100;
				repaint();
			}
		}
		else if ( (mousex >= 250) && (mousex <= 261) && (mousey >= 551) && (mousey <= 562) )
		{
			if ( player_list[tradepartner].resources[2] >= -trade[2]+100 )
			{
				trade[2]-=100;
				repaint();
			}
		}
		else if ( (mousex >= 250) && (mousex <= 261) && (mousey >= 568) && (mousey <= 579) )
		{
			if ( player_list[tradepartner].resources[3] >= -trade[3]+100 )
			{
				trade[3]-=100;
				repaint();
			}
		}
		// end trade button
		else if ( (mousex >= 191) && (mousex <= 202) && (mousey >= 531) && (mousey <= 542) )
		{
			boolean hasadjacent = false;

			for (int col=0; col < lot_cols; col++)
			{
				for (int row=0; row< lot_rows; row++)
				{
					if ( instance_list[col][row].owner == cur_turn )
					{
						if (check_adjacent(col,row,tradepartner))
						{
							hasadjacent = true;
						}
					}
				}
			}

			if ( active_effect == 1)
			{
				hasadjacent = true;
			}

			if ((hasadjacent) && (globalize_num > 0))
			{
				player_list[cur_turn].resources[0] -= trade[0];
				player_list[cur_turn].resources[1] -= trade[1];
				player_list[cur_turn].resources[2] -= trade[2];
				player_list[cur_turn].resources[3] -= trade[3];
				player_list[tradepartner].resources[0] += trade[0];
				player_list[tradepartner].resources[1] += trade[1];
				player_list[tradepartner].resources[2] += trade[2];
				player_list[tradepartner].resources[3] += trade[3];
				globalize_num--;
			}
			trade[0]=0;
			trade[1]=0;
			trade[2]=0;
			trade[3]=0;
			tradepartner=0;
			repaint();
		}
		// BANK-loan paying buttons
		else if ( (mousex >= 405) && (mousex <= 416) && (mousey >= 343) && (mousey <= 354) )
		{
			if ( ke.getButton() == 1 )			// when left mouse button is clicked
			{
				if (player_list[cur_turn].resources[0] >= (paying[0]/exchange[0]+1)*100)
				{
					if (total_paying()+exchange[0] > loan[cur_turn-1])
					{
						paying[0]+=(int) (loan[cur_turn-1]-total_paying())*100/exchange[0];
					}
					else
					{
						paying[0]+=exchange[0];
					}
				}
			}
			else if ( ke.getButton() == 3 )
			{
				if ( paying[0] > 0 )
				{
					paying[0]-=exchange[0];
				}
			}
			repaint();
		}
		else if ( (mousex >= 427) && (mousex <= 438) && (mousey >= 343) && (mousey <= 354) )
		{
			if ( ke.getButton() == 1 )			// when left mouse button is clicked
			{
				if (player_list[cur_turn].resources[1] >= (paying[1]/exchange[1]+1)*100)
				{
					if (total_paying()+exchange[1] > loan[cur_turn-1])
					{
						paying[1]+=(int) (loan[cur_turn-1]-total_paying())*100/exchange[1];
					}
					else
					{
						paying[1]+=exchange[1];
					}
				}
			}
			else if ( ke.getButton() == 3 )
			{
				if ( paying[1] > 0 )
				{
					paying[1]-=exchange[1];
				}
			}
			repaint();
		}
		else if ( (mousex >= 449) && (mousex <= 460) && (mousey >= 343) && (mousey <= 354) )
		{
			if ( ke.getButton() == 1 )			// when left mouse button is clicked
			{
				if (player_list[cur_turn].resources[2] >= (paying[2]/exchange[2]+1)*100)
				{
					if (total_paying()+exchange[2] > loan[cur_turn-1])
					{
						paying[2]+=(int) (loan[cur_turn-1]-total_paying())*100/exchange[2];
					}
					else
					{
						paying[2]+=exchange[2];
					}
				}
			}
			else if ( ke.getButton() == 3 )
			{
				if ( paying[2] > 0 )
				{
					paying[2]-=exchange[2];
				}
			}
			repaint();
		}
		else if ( (mousex >= 471) && (mousex <= 482) && (mousey >= 343) && (mousey <= 354) )
		{
			if ( ke.getButton() == 1 )			// when left mouse button is clicked
			{
				if (player_list[cur_turn].resources[3] >= (paying[3]/exchange[3]+1)*100)
				{
					if (total_paying()+exchange[3] > loan[cur_turn-1])
					{
						paying[3]+=(int) (loan[cur_turn-1]-total_paying())*100/exchange[3];
					}
					else
					{
						paying[3]+=exchange[3];
					}
				}
			}
			else if ( ke.getButton() == 3 )
			{
				if ( paying[3] > 0 )
				{
					paying[3]-=exchange[3];
				}
			}
			repaint();
		}
		// official pay loan
		else if ( (mousex >= 490) && (mousex <= 501) && (mousey >= 343) && (mousey <= 364) )
		{
			loan[cur_turn-1]-=total_paying();
			for ( int rn=0;rn<4;rn++)
			{
				player_list[cur_turn].resources[rn]-=(paying[rn]/exchange[rn])*100;
				paying[rn] = 0;
			}
			repaint();
		}

		// end turn button
		if ( (mousex >= 456) && (mousex <= 529) && (mousey >= 465) && (mousey <= 487) )
		{
			if ( numactions == 0)
			{
				apply_effects();
			}
			change_turn();
			repaint();
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
		Dimension d = this.getSize();
		int p_count = 0;				// player counter

		g.setColor(Color.WHITE);
		g.fillRect(0,0,d.width,d.height);
		// game board backing
		g.setColor(Color.BLACK);

	/* DRAW GAMEBOARD */
		for ( int col = 0; col < lot_cols; col++ )
		{
			for ( int row = 0; row < lot_rows; row++ )
			{
				// draw coloured background
				g.setColor(player_list[instance_list[col][row].owner].usercolour);
				g.fillRect ( gameboard_x+(lot_size+lot_space)*col-1,
							gameboard_y+(lot_size+lot_space)*row-1,
							lot_size+lot_space, lot_size+lot_space);
				// draw tile itself
				if ( instance_list[col][row].sheep == 5 )
				{
					g.drawImage ( 	tile_list[ (instance_list[col][row].TI_ref/2) + INDUSTRY_IMG ].img_file,
									gameboard_x+(lot_size+lot_space)*col,
									gameboard_y+(lot_size+lot_space)*row,
									lot_size, lot_size, this );
				}
				else
				{
					g.drawImage ( 	tile_list[ instance_list[col][row].TI_ref ].img_file,
									gameboard_x+(lot_size+lot_space)*col,
									gameboard_y+(lot_size+lot_space)*row,
									lot_size, lot_size, this );
				}
			}
		}

		g.setColor(Color.BLACK);
	/* DRAW SELECTED TILE */
		g.drawImage ( tile_list[ instance_list[disp_col][disp_row].owner+BG_IMG ].img_file, 420, 55, 40, 30, this );
		if ( instance_list[disp_col][disp_row].sheep == 5 )
		{
			g.drawImage ( tile_list[ (instance_list[disp_col][disp_row].TI_ref/2) + INDUSTRY_IMG + 4].img_file, 420, 55, 40, 30, this );
		}
		else
		{
			g.drawImage ( tile_list[ instance_list[disp_col][disp_row].TI_ref+1 ].img_file, 420, 55, 40, 30, this );
		}
		// text stuff
		g.setFont( new Font("SansSerif", Font.BOLD, 11) ) ;
		if ( instance_list[disp_col][disp_row].sheep == 5 )
		{
			g.drawString ( tile_list[ (instance_list[disp_col][disp_row].TI_ref/2) + INDUSTRY_IMG].tilename, 420, 100 );
		}
		else
		{
			g.drawString ( tile_list[ instance_list[disp_col][disp_row].TI_ref ].tilename, 420, 100 );
		}
		g.drawString ( "[" + disp_col + "," + disp_row + "]", 470, 75 );							// coordinates
		g.drawString ( player_list[instance_list[disp_col][disp_row].owner].username, 420, 44 );	// username
		for ( int sheepnum = 0; sheepnum < instance_list[disp_col][disp_row].sheep; sheepnum++ )
		{
			if ( sheepnum < 4 )
			{
				g.drawImage ( tile_list[ SHEEP_IMG ].img_file, 420+(22)*sheepnum, 110, 20, 20, this );
			}
		}
		// if it has been industrialized
		if ( instance_list[disp_col][disp_row].sheep == 5 )
		{
			g.drawRect (394,109,21,21);
			g.drawImage ( tile_list[ (instance_list[disp_col][disp_row].TI_ref/2) + INDUSTRY_IMG ].img_file, 395, 110, 20, 20, this );
		}

	/* TILE OPTIONS */
		if ( dif_per() == false )
		{
			// if you can populate it further
			if ( instance_list[disp_col][disp_row].sheep < 4 && populate_num > 0 )
			{
				g.drawRect (410,140,21,21);
				g.drawImage ( tile_list[ SHEEP_IMG ].img_file, 411, 141, this);
				g.drawString ( "Populate", 440, 155 );
			}
			// if you can industrialize it
			else if ( instance_list[disp_col][disp_row].sheep == 4 && populate_num > 0 )
			{
				g.drawRect (410,140,21,21);
				g.drawImage ( tile_list[ (instance_list[disp_col][disp_row].TI_ref/2) + INDUSTRY_IMG ].img_file,411, 141,20,20, this);
				g.drawString ( "Industrialize", 440, 155 );
			}
		}
		// TRADE
		else if ( instance_list[disp_col][disp_row].owner > 0 )
		{
			if ( instance_list[disp_col][disp_row].owner != tradepartner && globalize_num>0 )
			{
				g.drawRect(505, 60, 21, 21);
				g.drawImage ( tile_list[29].img_file, 506, 61, this );
				g.drawString ( "Globalize", 535, 75 );
			}
		}

	/* CHARACTER INFO */
		g.drawString ( player_list[cur_turn].username, 50, 395 );
		g.setColor( player_list[cur_turn].usercolour);
		g.fillRect (100, 385, 100, 10 );
		g.setColor(Color.BLACK);
		for ( int rn=0; rn<4; rn++ )
		{
			g.drawImage ( tile_list[ RESOURCE_IMG+rn ].img_file, 55, 401+13*rn, 11, 11, this );
			g.drawString ("[" + player_list[cur_turn].resources[rn] + "] " + resource_names[rn], 70, 410+13*rn );
		}

		for ( int pn = 1; pn <= numofplayers; pn++ )
		{
			g.drawString ( player_list[pn].username, 250+60*p_count, 395 );
			g.setColor( player_list[pn].usercolour);
			g.fillRect (248+60*p_count, 401, 5, 50 );
			g.setColor(Color.BLACK);
			for ( int rn=0; rn<4; rn++ )
				{
				g.drawImage ( tile_list[ RESOURCE_IMG+rn ].img_file, 255+60*p_count, 401+13*rn, 11, 11, this );
				g.drawString ("[" + player_list[pn].resources[rn] + "] ", 270+60*p_count, 410+13*rn );
			}
			p_count++;
		}

	/* CARD DISPLAY */
		if ( displaycard )
		{
			g.drawString ( card_list[cardnum].cardname, 400, 180 );
			g.drawImage ( tile_list[ cardnum+CARD_IMG ].img_file, 400, 190, this );
			printString ( g, card_list[cardnum].explanation, 460, 200, 11);
		}

	/* BANK DISPLAY */
		if ( loan[cur_turn-1] > 0 )
		{
			g.drawString ( "Bank Info", 400, 315);
			temp = 0;
			for ( int rn=0;rn<4;rn++ )
			{
				temp += player_list[cur_turn].resources[rn]*exchange[rn]/100;
				g.drawImage ( tile_list[RESOURCE_IMG+rn].img_file, 405+22*rn, 343, this);
			}
			g.drawString ( "Total: " + temp, 405, 327);
			g.drawString ( "Loan: " + loan[cur_turn-1], 405, 339);
			g.drawString ( "Paying: " + total_paying(), 405, 366);
			g.drawRect(490,343,21,21);
			g.drawImage(tile_list[30].img_file, 491,344,this);
		}

	/* EFFECT BUTTONS */
		switch ( active_effect )
		{
			case 1://globalization
				break;
			case 2: //take tile
				g.drawRect ( 240, 465, 100, 22 );
				g.drawString ( "Take this Tile?", 250, 480 );
				effectmsg = "Choose a tile adjacent to yours.";
				break;
			case 3: //take sheep A
				g.drawRect ( 240, 465, 100, 22 );
				g.drawString ( "Take Sheep?", 255, 480 );
				effectmsg = "Choose a tile with your desired \nsheep.";
				break;
			case 50: //take sheep B
				g.drawRect ( 240, 465, 100, 22 );
				g.drawString ( "Place Sheep?", 255, 480 );
				effectmsg = "Choose your tile you'd like to \nput the sheep on.";
				break;
			case 4:
				g.drawRect ( 240, 465, 100, 22 );
				g.drawString ("Choose Tile", 255, 480 );
				effectmsg = "Choose your tile with your \ndesired resource";
				break;
		}

	printString ( g, effectmsg, 50, 480, 11 );

	/* TURN BUTTONS */
		if ( cardspicked < decksize )
		{
			if ( randomize_num > 0 && numactions == 0)
			{
				g.drawRect ( 363, 465, 73, 22 );
				g. drawString ( "Randomize", 371, 480 );
			}
		}
		g.drawRect ( 456, 465, 73, 22 );
		g. drawString ( "End Turn", 470, 480 );

	/* TRADE BUTTONS */
		if ( tradepartner > 0 )
		{
			g.drawString ( "Globalize [" + player_list[cur_turn].username + "]", 50, 510 );
			g.drawImage ( tile_list[29].img_file, 191, 531, this );
			g.drawString ( "Globalize [" + player_list[tradepartner].username + "]", 250, 510);
			for ( int rn=0; rn<4; rn++ )
				{
				g.drawImage ( tile_list[ RESOURCE_IMG+rn ].img_file, 50, 517+17*rn, 11, 11, this );
				if ( trade[rn] <= 0 )
				{
					g.drawString ( "[0]", 70, 525+17*rn );
					g.drawString ( "[" + (-trade[rn]) + "]", 270, 525+17*rn );
				}
				else
				{
					g.drawString ( "[" + trade[rn] + "]", 70, 525+17*rn );
					g.drawString ( "[0]", 270, 525+17*rn );
				}
				g.drawImage ( tile_list[ RESOURCE_IMG+rn ].img_file, 250, 517+17*rn, 11, 11, this );
			}
		}
	};

	public static void printString (Graphics g, String st, int x, int y, int fontSize)
	{
		String s=st;
		int i=1, ln=0;
		while (i>0)
		{
			i=s.indexOf("\n",0);
			if (i>1){
				g.drawString(s.substring(0,i), x, y+ln*(fontSize+2));
				s=s.substring(i+1,s.length());
				ln++;
			}else g.drawString( s, x, y+ln*(fontSize+2) );
		}
	}	// end printString

	// dice rolling simulation
	public int roll_die ( int sides )
	{
		return ((int) Math.round(Math.random()*sides - 0.5));
	}

	// check if the square is adjacent to a square of a certain player
	public boolean check_adjacent (int col, int row, int player)
	{
		boolean is_adjacent = false;
		if ( col > 0 && instance_list[col-1][row].owner == player )
		{
			is_adjacent = true;
		}
		if ( col < (lot_cols-1) && instance_list[col+1][row].owner == player )
		{
			is_adjacent = true;
		}
		if ( row > 0 && instance_list[col][row-1].owner == player )
		{
			is_adjacent = true;
		}
		if ( row < (lot_rows-1) && instance_list[col][row+1].owner == player )
		{
			is_adjacent = true;
		}
		return is_adjacent;
	}

	public void change_turn ()
	{
		int totrsrc;

		if ( cur_turn > 0 )
		{
			resource_add();
			loan[cur_turn-1]*=1+(interestrate/100);
		}

		// change the turn
		cur_turn++;			// next player's turn
		if ( cur_turn > numofplayers )	// loop back to player 1 if at 5
		{
			cur_turn = 1;
		}

		effectmsg = "";

		temp = 0;
		if ( tilesexplored == lot_cols*lot_rows )
		{
			for (int pnum = 1; pnum<=5;pnum++)
			{
				totrsrc = (int) (player_list[pnum].resources[0]*exchange[0]+player_list[pnum].resources[1]*exchange[1]
					+player_list[pnum].resources[2]*exchange[2]+player_list[pnum].resources[3]*exchange[3]);
				if ( totrsrc > temp )
				{
					temp = totrsrc;
					effectmsg = player_list[pnum].username + " is the winner.";

				}
			}
		}

		// start of turn
		explore_num = 1;
		populate_num =1;
		randomize_num = 1;
		globalize_num = 1;

		// reset variables
		tradepartner = 0;
		paying[0] = 0;
		paying[1] = 0;
		paying[2] = 0;
		paying[3] = 0;
		numactions = 0;
		active_effect = 0;
		repaint();

		rsrc1_add = 100;
		rsrc2_add = 100;
		rsrc3_add = 100;
		rsrc4_add = 100;

		trade[0]=0;
		trade[1]=0;
		trade[2]=0;
		trade[3]=0;

		displaycard = false;
	}

	public void resource_add ()
	{
		int num_add = 100;

		// goes through each cell
		for ( int col = 0; col < lot_cols; col++ )
		{
			for ( int row = 0; row < lot_rows; row++ )
			{
				// if the cell is owned by the current player
				if ( instance_list[col][row].owner == cur_turn )
				{
					switch ( instance_list[col][row].TI_ref )
					{
						case 2:		num_add = rsrc1_add;
									break;
						case 4:		num_add = rsrc2_add;
									break;
						case 6:		num_add = rsrc3_add;
									break;
						case 8:		num_add = rsrc4_add;
									break;
					}

					if ( num_add > 0 )
					{
						switch (instance_list[col][row].sheep)
						{
							case 5:		num_add += 100;
							case 4:		num_add += 25;
							case 3:		num_add += 50;
							case 2:		num_add += 25;
										break;
						}
					}
					player_list[cur_turn].resources[ (instance_list[col][row].TI_ref/2)-1 ] += num_add;
				}
			}
		}
	}

	public void draw_card ()
	{
		if ( numactions == 0 )
		{
			if ( cardspicked < decksize )
			{
				cardnum = roll_die(CARD_LNGTH);

				// while more than 0 is left in the deck.
				while ( card_list[cardnum].played == 0 )
				{
					cardnum = roll_die(CARD_LNGTH);
				}

				card_list[cardnum].played--;	// take 1 out of the deck
				cardspicked++;					// total cards drawn
				randomize_num--;				// action used
				numactions++;					// number of actions done that turn

				// create an effect
				effect_list.addLast ( new Effect_Info ( card_list[cardnum].removed, card_list[cardnum].rsrc1, card_list[cardnum].rsrc2,
									card_list[cardnum].rsrc3, card_list[cardnum].rsrc4, card_list[cardnum].exp_add, card_list[cardnum].pop_add,
									card_list[cardnum].ind_add, card_list[cardnum].glo_add, card_list[cardnum].affected, card_list[cardnum].effect ) );

				// set card display flag to on
				displaycard = true;

				repaint();
			}
		}
	}

	public void apply_effects ()
	{
		Effect_Info temp;
		boolean		performed = false;

		for ( int enum = 0; enum<effect_list.size(); enum++ )
		{
			temp = (Effect_Info) effect_list.get(enum);

			// if it affects them
			if ( (temp.affected == 0) || ( (temp.affected > 1) && ( temp.affected-1 == cur_turn ) ) )
			{
				explore_num += temp.exp_add;
				populate_num += temp.pop_add;
				globalize_num += temp.glo_add;
				rsrc1_add = (int) Math.floor(rsrc1_add*temp.rsrc1);
				rsrc2_add = (int) Math.floor(rsrc2_add*temp.rsrc2);
				rsrc3_add = (int) Math.floor(rsrc3_add*temp.rsrc3);
				rsrc4_add = (int) Math.floor(rsrc4_add*temp.rsrc4);

				// set it as the active effect
				active_effect = temp.effect;
			}
			else if ( temp.affected == 1 )
			{
				temp.affected = 0;
			}
			else if ( temp.affected == -1 )
			{
				effect_list.set(enum, new Effect_Info ( temp.removed, temp.rsrc1, temp.rsrc2, temp.rsrc3, temp.rsrc4, temp.exp_add,
										 temp.pop_add, temp.ind_add, temp.glo_add, cur_turn+1, temp.effect ));
			}

			if (temp.effect == 5)
			{
				interestrate +=5;
			}
			else if (temp.effect == 6)
			{
				tempcol = -1;
				for ( int col=0;col<lot_cols;col++ )
				{
					for ( int row=0;row<lot_rows; row++ )
					{
						if ( instance_list[col][row].owner == cur_turn )
						{
							if (tempcol>-1)
							{
								if ( instance_list[col][row].sheep > instance_list[tempcol][temprow].sheep && (instance_list[col][row].sheep < 4))
								{
									tempcol = col;
									temprow = row;
								}
							}
							else
							{
								tempcol = col;
								temprow = row;
							}
						}
					}
				}
				instance_list[tempcol][temprow].sheep = 1;
			}
			else if (temp.effect == 7)
			{
				exchange[0] += (roll_die(5)+1);
			}
			else if (temp.effect == 8)
			{
				exchange[1] += (roll_die(5)+1);
			}
			else if (temp.effect == 9)
			{
				exchange[2] += (roll_die(5)+1);
			}
			else if (temp.effect == 10)
			{
				exchange[3] += (roll_die(5)+1);
			}
			else if (temp.effect == 11)
			{
				exchange[0] -= (roll_die(5)+1);
			}
			else if (temp.effect == 12)
			{
				exchange[1] -= (roll_die(5)+1);
			}
			else if (temp.effect == 13)
			{
				exchange[2] -= (roll_die(5)+1);
			}
			else if (temp.effect == 14)
			{
				exchange[3] -= (roll_die(5)+1);
			}

			// remove it from the effects list if it is done
			temp.removed--;
			if ( temp.removed == 0 )
			{
				effect_list.remove(enum);
			}
		}
	}

	// counts the number of square that a certain player owns
	public int count_owned ( int player )
	{
		int num = 0;

		// goes through each cell
		for ( int col = 0; col < lot_cols; col++ )
		{
			for ( int row = 0; row < lot_rows; row++ )
			{
				if ( instance_list[col][row].owner == player )
				{
					num++;
				}
			}
		}

		return num;
	}

	public int total_paying ()
	{
		return (int) (paying[0]+paying[1]+paying[2]+paying[3]);
	}

	public void end_action ()
	{
		numactions++;

		if ( numactions == 1)
		{
			apply_effects();
		}
	}

	public boolean dif_per ()
	{
		boolean istrue = true;

		if (instance_list[disp_col][disp_row].owner == cur_turn)
		{
			istrue = false;
		}

		return istrue;
	}
}