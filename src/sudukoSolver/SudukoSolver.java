package sudukoSolver;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

@SuppressWarnings("serial")
public class SudukoSolver extends JPanel
{

	static JFrame parent;

	int[][] table; //3x3 first second 3x3 -- 81 size

	boolean[][] lockedTable; // Tells which indices are part of the puzzle

	boolean[][][] dismissed; // Determines which numbers may belong to this slot 3x3x3 array. Gross.

	int sS = -1, xS = -1, yS = -1;

	boolean dismissing = false;

	final int size = 3, rectSize = 150;

	Color lightGray = new Color(240, 240, 240), cyan = new Color(195, 230, 227), teal = new Color(115, 193, 185),
			pink = new Color(255, 132, 132), red = new Color(255, 73, 73);

	public SudukoSolver()
	{
		MouseInputAdapter mouseAdapter = new MouseInputAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				super.mousePressed(e);
				updatePos(e);
			}

			@Override
			public void mouseDragged(MouseEvent e)
			{
				super.mouseDragged(e);
				updatePos(e);
			}

			public void updatePos(MouseEvent e)
			{
				final int xA = getWidth() / 2 - (rectSize * size) / 2, yA = getWidth() / 2 - (rectSize * size) / 2;

				if (e.getX() >= xA && e.getX() <= xA + rectSize * size && e.getY() >= yA && e.getY() <= yA + rectSize * size)
				{
					final int square = (e.getX() - xA) / rectSize + ((e.getY() - yA) / rectSize) * size;
					final int smallRect = (rectSize / size);

					sS = square;
					xS = (e.getX() - xA) / smallRect;
					yS = (e.getY() - yA - smallRect / 2) / smallRect;
				}
				else
				{
					sS = -1;
					xS = -1;
					yS = -1;
				}

				repaint();
			}
		};

		parent.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				super.keyPressed(e);

				boolean selected = sS != -1 && !lockedTable[sS][xS % size + (yS % size) * size];

				switch (e.getKeyCode())
				{
				case KeyEvent.VK_D:
					
					for (int x = 0; x < size * size; x++)
					{
						for (int y = 0; y < size * size; y++)
						{
							dismiss(x, y);
						}
					}
					
					
					break;
				case KeyEvent.VK_SHIFT:

					dismissing = !dismissing;

					break;
				case KeyEvent.VK_BACK_SPACE:

					if (selected)
					{
						if (dismissing)
						{
							for (int i = 0; i < dismissed[sS][xS % size + (yS % size) * size].length; i++)
							{
								dismissed[sS][xS % size + (yS % size) * size][i] = false;
							}
						}
						else
						{
							table[sS][xS % size + (yS % size) * size] = 0;
						}
					}

					break;
				case KeyEvent.VK_UP:

					if (yS > 0)
					{
						yS--;
					}
					else
					{
						yS = size * size - 1;
					}

					break;
				case KeyEvent.VK_DOWN:

					if (yS < size * size - 1)
					{
						yS++;
					}
					else
					{
						yS = 0;
					}

					break;
				case KeyEvent.VK_LEFT:

					if (xS > 0)
					{
						xS--;
					}
					else
					{
						xS = size * size - 1;
					}

					break;
				case KeyEvent.VK_RIGHT:

					if (xS < size * size - 1)
					{
						xS++;
					}
					else
					{
						xS = 0;
					}

					break;
				default:

					if (e.getKeyChar() > 48 && e.getKeyChar() <= 57 && selected)
					{
						if (dismissing)
						{
							dismissed[sS][xS % size + (yS % size) * size][Integer.parseInt(String.valueOf(e.getKeyChar())) - 1] = !dismissed[sS][xS % size + (yS % size) * size][Integer.parseInt(String.valueOf(e.getKeyChar())) - 1];
						}
						else
						{
							table[sS][xS % size + (yS % size) * size] = Integer.parseInt(String.valueOf(e.getKeyChar()));
						}
					}
				}

				if (xS < 0 || yS < 0)
				{
					sS = -1;
				}
				else
				{
					sS = xS / size + (yS / size) * size;
				}

				repaint();
			}
		});

		parent.addMouseListener(mouseAdapter);
		parent.addMouseMotionListener(mouseAdapter);

		table = new int[][] {
				new int[] { 0, 1, 0, 6, 0, 0, 0, 0, 0 },
				new int[] { 7, 0, 6, 3, 0, 9, 0, 2, 0 },
				new int[] { 0, 5, 9, 2, 0, 1, 7, 0, 0 },
				new int[] { 1, 0, 0, 0, 6, 0, 2, 0, 3 },
				new int[] { 0, 0, 0, 5, 0, 8, 0, 0, 0 },
				new int[] { 8, 0, 7, 0, 4, 0, 0, 0, 5 },
				new int[] { 0, 0, 1, 5, 0, 7, 8, 3, 0 },
				new int[] { 0, 5, 0, 6, 0, 4, 2, 0, 7 },
				new int[] { 0, 0, 0, 0, 0, 8, 0, 1, 0 }
		};

		setupTable();

		repaint();
	}

	public static void main(String[] args)
	{
		parent = new JFrame("Suduko Solver");

		parent.add(new SudukoSolver());
		parent.setVisible(true);
		parent.setSize(700, 700);
		parent.setResizable(false);
		parent.setLocationRelativeTo(null);
		parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setupTable()
	{
		lockedTable = new boolean[size * size][];
		dismissed = new boolean[size * size][][];

		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				final int square = x * size + y;

				lockedTable[square] = new boolean[size * size];
				dismissed[square] = new boolean[size * size][];

				for (int x1 = 0; x1 < size; x1++)
				{
					for (int y1 = 0; y1 < size; y1++)
					{
						lockedTable[square][x1 * size + y1] = table[square][x1 * size + y1] == 0 ? false : true;
						dismissed[square][x1 * size + y1] = new boolean[size * size];
					}
				}
			}
		}
	}

	public void dismiss(int x, int y)
	{
		final int square = (x / size) * size + y / size;

		for (int x1 = 0; x1 < size; x1++)
		{
			for (int y1 = 0; y1 < size; y1++)
			{
				checkAndDismiss(x, y, square, square, x1, y1);
			}
		}

		for (int x1 = 0; x1 < size * size; x1++)
		{
			final int square2 = (x1 / size) * size + y / size;

			checkAndDismiss(x, y, square, square2, x1 % size, y % size);
		}

		for (int y1 = 0; y1 < size * size; y1++)
		{
			final int square2 = (x / size) * size + y1 / size;

			checkAndDismiss(x, y, square, square2, x % size, y1 % size);
		}
		
		final boolean[] allDis = dismissed[square][(x % size) * size + (y % size)];
		int dissed = 0, remainder = 0;
		
		for (int i = 0; i < allDis.length; i++)
		{
			if (allDis[i])
			{
				dissed++;
			}
			else
			{
				remainder = i;
			}
		}
		
		if (dissed == allDis.length - 1)
		{
			table[square][(x % size) * size + (y % size)] = remainder + 1;
			dismissed[square][(x % size) * size + (y % size)] = new boolean[size * size];
		}
	}

	public void checkAndDismiss(int x, int y, int square, int square2, int x1, int y1)
	{
		final int val = table[square2][x1 * size + y1];

		if (table[square][(x % size) * size + (y % size)] == 0 && (x % size != x1 || y % size != y1 || square2 != square) && val != 0)
		{
			dismissed[square][(x % size) * size + (y % size)][val - 1] = true;
		}
	}

	public void drawRect(Graphics2D g)
	{
		final int xA = getWidth() / 2 - (rectSize * size) / 2, yA = getWidth() / 2 - (rectSize * size) / 2;

		g.setColor(Color.black);
		g.drawRect(xA, yA, rectSize * size, rectSize * size);

		for (int i = 0; i < 4; i++)
		{
			if (i == 1)
			{
				g.setColor(dismissing ? pink : cyan);
			}
			else if (i == 2)
			{
				g.setColor(Color.black);
			}

			for (int x = 0; x < size; x++)
			{
				for (int y = 0; y < size; y++)
				{
					if (i == 1 && sS != -1)
					{
						g.fillRect(xA + xS * (rectSize / size), yA, rectSize / size, rectSize * size);
						g.fillRect(xA, yA + yS * (rectSize / size), rectSize * size, rectSize / size);
					}
					else if (i == 2)
					{
						drawRect(g, x * size + y, table[x * size + y]);
					}
					else
					{
						final int square = x * size + y;
						final int xD = xA + rectSize * (square % size), yD = yA + rectSize * (square / size);
						final boolean white = square % 2 == 0;

						if (i == 0)
						{
							g.setColor(!white ? lightGray : Color.white);
							g.fillRect(xD, yD, rectSize, rectSize);
						}

						g.drawRect(xD, yD, rectSize, rectSize);
					}
				}
			}

			if (i == 1 && sS != -1)
			{
				g.setColor(dismissing ? red : teal);
				g.fillRect(xA + xS * (rectSize / size), yA + yS * (rectSize / size), rectSize / size, rectSize / size);
			}
		}
	}

	public void drawRect(Graphics2D g, final int square, final int[] rect)
	{
		final int xA = getWidth() / 2 - (rectSize * size) / 2, yA = getWidth() / 2 - (rectSize * size) / 2;
		final int xD = xA + rectSize * (square % size), yD = yA + rectSize * (square / size);
		final FontMetrics fontMetrics = g.getFontMetrics(g.getFont());

		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				final int square2 = x * size + y;
				final int numb = rect[square2], smallRect = rectSize / size;

				g.setColor(Color.gray);
				g.drawRect(xD + smallRect * (square2 % size), yD + smallRect * (square2 / size), smallRect, smallRect);

				if (lockedTable[square][square2])
				{
					g.setColor(Color.black);
				}
				else
				{
					g.setColor(Color.blue);
				}

				boolean isEmpty = true;

				for (boolean b : dismissed[square][square2])
				{
					if (b == true)
					{
						isEmpty = false;
					}
				}

				final int xDS = xD + smallRect * (square2 % size), yDS = yD + smallRect * (square2 / size);

				if (isEmpty && numb != 0)
				{
					g.drawString(String.valueOf(numb), xDS + smallRect / 2 - fontMetrics.stringWidth(String.valueOf(numb)) / 2, yDS + smallRect / 2 + getFont().getSize() / 2);
				}
				else if (!isEmpty)
				{
					g.setFont(new Font("Calibri", 0, 15));

					final FontMetrics newMetrics = g.getFontMetrics();

					for (int x1 = 0; x1 < size; x1++)
					{
						for (int y1 = 0; y1 < size; y1++)
						{
							if (dismissed[square][square2][x1 * size + y1])
							{
								final String string = String.valueOf(x1 * size + y1 + 1);

								g.drawString(string, xDS + (smallRect / size) * y1 + (smallRect / size) / 2 - newMetrics.stringWidth(string) / 2, yDS + (smallRect / size) * x1 + (smallRect / size) / 2 + g.getFont().getSize() / 2);
							}
						}
					}

					g.setFont(new Font("Calibri", 0, 20));
				}
			}
		}

		g.setColor(Color.black);
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		g.setFont(new Font("Calibri", 0, 20));
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, getWidth(), getHeight());
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawRect((Graphics2D) g);
	}

}
