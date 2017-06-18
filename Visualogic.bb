;==================================================================
;Project Title: Visualogic
;Author:        Vladislav (Borzunov) Trubicin aka ObelardO (c) 2014
;Email:         Obelardos@gmail.com
;Notes:         Nodes-based program for visual and interactive 
;               designing and prototyping logical equivalences           
;==================================================================
 
Include "code\Engine.bb"

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ DATA ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Const VIEWMODE_GRID  = 0
Const VIEWMODE_NODES = 1

Const NODE_KIND_USER    = 0
Const MODE_KIND_BUTTON  = 1
Const MODE_KIND_LAMP    = 2
Const NODE_MAX_RECEIVER = 15

Const WINDOW_WIDTH  = 1280
Const WINDOW_HEIGHT = 720

Global SKETCH_CENTER_X     = WINDOW_WIDTH  * 0.5
Global SKETCH_CENTER_Y     = WINDOW_HEIGHT * 0.5
Global SKETCH_CENTER_AIM_X = SKETCH_CENTER_X
Global SKETCH_CENTER_AIM_Y = SKETCH_CENTER_Y
Global SKETCH_CELL_SIZE    = 25
Global SKETCH_ZOOM         = 100
Global SKETCH_ZOOM_CURRENT = 100
Global SKETCH_VIEWMODE
Global MOUSE_X, MOUSE_Y, MOUSE_HIT_1, MOUSE_DOWN_1

Global PROTOTYPE_SELECTED.TYPE_NODE_PROTOTYPE
Global NODE_SELECTED.TYPE_NODE
Global NODE_TRANSMITTER.TYPE_NODE
Global NODE_MOVING
Global NODE_MENU_SCROLL, NODE_MENU_SCROLL_CUR
Global NODE_BLOCK_CONNECTIONS
Global NODE_CICLE_COUNT

Global FONT_LOGO  
Global FONT_LARGE 
Global FONT_SMALL 
Global FONT_STAND

Type TYPE_NODE_PROTOTYPE
	Field Name$
	Field Discription$
	Field Result%[15]
	Field Kind%
	Field Inputs%
	Field Image
End Type

Type TYPE_NODE 
	Field PROTOTYPE.TYPE_NODE_PROTOTYPE
	Field RECEIVER.TYPE_NODE[NODE_MAX_RECEIVER]
	Field PosY%, PosX%, Width%, Height%
	Field Value%
	Field InputSlot%[3]
	Field InputID[NODE_MAX_RECEIVER]
End Type

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ START ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

InitApp()
InitPrototypes()
InitLogo()

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ LOOP ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

While Not xWinMessage("WM_CLOSE"); Or xKeyHit(KEY_ESCAPE))
	xCls
	UpdateMouse()
	UpdateGrid()
	UpdateNodes()
    UpdateNodesMenu()
	UpdateUI()
Wend

End

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ INIT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Function InitApp()
	xSetEngineSetting("Splash::TilingTime", 0)
	xSetEngineSetting("Splash::AfterTilingTime", 0)

	xAppTitle "Visualogic"
	xGraphics3D WINDOW_WIDTH, WINDOW_HEIGHT, 32, 0, 1
	
	FONT_LOGO  = xLoadFont("Century Gothic", 48)
	FONT_LARGE = xLoadFont("Century Gothic", 24)
	FONT_SMALL = xLoadFont("Century Gothic", 16)
	FONT_STAND = xLoadFont("Century Gothic", 12)
	xClsColor 32, 32, 32, 0
End Function 

Function InitLogo()
	Local logostep, i
	xSetFont FONT_LOGO
	While Not(logofinished)

	xCls

		Select logostep
			Case 0
				i = i + 1
				If i <= 100 Alpha = i
				xColor 255, 255, 255, Alpha
				xText (WINDOW_WIDTH - xStringWidth("V i S U A L O G I C")) * 0.5, WINDOW_HEIGHT * 0.5 - 100,  "V i S U A L O G I C"
				
				If i = 100
					logostep = 1
					i = 0
				End If
				
			Case 1
				xSetFont FONT_LOGO
				xColor 255, 255, 255, 100
				xText (WINDOW_WIDTH - xStringWidth("V i S U A L O G I C")) * 0.5, WINDOW_HEIGHT * 0.5 - 100,  "V i S U A L O G I C"
			
				i = i + 1
				If i <= 50 Alpha = i
				xColor 255, 255, 255, Alpha

				xSetFont FONT_SMALL
				xText (WINDOW_WIDTH - xStringWidth("visual. interactive. simple.")) * 0.5, WINDOW_HEIGHT * 0.5 + (100 - Alpha) - 50,  "visual. interactive. simple."

				xColor 255, 255, 255, Alpha * 0.5
				xSetFont FONT_SMALL
				xText (WINDOW_WIDTH - xStringWidth("Visualogic (c) 2014 Vladislav Trubicin aka ObelardO")) * 0.5, WINDOW_HEIGHT - 60,  "Visualogic (c) 2014 Vladislav Trubicin aka ObelardO"
				xText (WINDOW_WIDTH - xStringWidth("obelardos@gmail.com")) * 0.5, WINDOW_HEIGHT - 40,  "obelardos@gmail.com"
			
				If i = 100
				    logostep = 2
					i = 0
				End If
				
			Case 2
				xSetFont FONT_LOGO
				xColor 255, 255, 255, 100
				xText (WINDOW_WIDTH - xStringWidth("V i S U A L O G I C")) * 0.5, WINDOW_HEIGHT * 0.5 - 100,  "V i S U A L O G I C"
			
				xColor 255, 255, 255, 50
				xSetFont FONT_SMALL
				xText (WINDOW_WIDTH - xStringWidth("visual. interactive. simple.")) * 0.5, WINDOW_HEIGHT * 0.5 + (100 - 50) - 50,  "visual. interactive. simple."

				xColor 255, 255, 255, 25
				xSetFont FONT_SMALL
				xText (WINDOW_WIDTH - xStringWidth("Visualogic (c) 2014 Vladislav Trubicin aka ObelardO")) * 0.5, WINDOW_HEIGHT - 60,  "Visualogic (c) 2014 Vladislav Trubicin aka ObelardO"
				xText (WINDOW_WIDTH - xStringWidth("obelardos@gmail.com")) * 0.5, WINDOW_HEIGHT - 40,  "obelardos@gmail.com"

				i = i + 5
				xColor 32, 32, 32, i

				xRect 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 1

				If i = 255
					logofinished = True
				End If 
		End Select
	
	xFlip

	Wend
End Function

Function InitPrototypes()
	NodesFile$ = ReadFile("Nodes.csv")

	If NodesFile
		While Not(Eof(NodesFile))
			NodeSource$ = ReadLine(NodesFile)

			If NodeSource <> ""
	
				NodeName$ = StrCut(NodeSource, 1, ";")
				NodeDiscription$ = StrCut(NodeSource, 2, ";")
				NodeInputs = Int(StrCut(NodeSource, 3, ";"))
				NodeResult$ = StrCut(NodeSource, 4, ";")
	
				AddNewNodePrototype(NodeName, NodeDiscription, NodeResult, NodeInputs)

			End If
		Wend
	End If

	AddNewNodePrototype("Button", "logical 0 or 1", "", 0, MODE_KIND_BUTTON)
	AddNewNodePrototype("Lamp", "show 0 or 1", "", 1, MODE_KIND_LAMP)
End Function

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ UPDATE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Function UpdateMouse()
	MOUSE_HIT_1  = xMouseHit(1)
	MOUSE_DOWN_1 = xMouseDown(1)
End Function

Function UpdateUI()
	xSetFont FONT_STAND
	xColor 255, 255, 255, 25
	xText 10, 10, "FPS: "  + xGetFPS()

	xFlip
End Function

Function UpdateNodesMenu()

	If xKeyHit(KEY_SPACE) Or xKeyHit(KEY_TAB)
		If SKETCH_VIEWMODE = VIEWMODE_GRID SKETCH_VIEWMODE = VIEWMODE_NODES Else SKETCH_VIEWMODE = VIEWMODE_GRID
	End If

	If SKETCH_VIEWMODE = VIEWMODE_NODES

		xColor 16, 16,16, 196
		xRect 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 1

		For PROTOTYPE.TYPE_NODE_PROTOTYPE = Each TYPE_NODE_PROTOTYPE

			NodePosX = (WINDOW_WIDTH - 950) * 0.5 + i * 250 - SKETCH_CENTER_X
			NodePosY = (WINDOW_HEIGHT - 600) + j * 200 - SKETCH_CENTER_Y + NODE_MENU_SCROLL

			MENU_MOUSE_X = xMouseX()
			MENU_MOUSE_Y = xMouseY()
			MENU_NODE_X = NodePosX + SKETCH_CENTER_X
			MENU_NODE_Y = NodePosY + SKETCH_CENTER_Y

			If MENU_MOUSE_X > MENU_NODE_X And MENU_MOUSE_X < MENU_NODE_X + 200
			If MENU_MOUSE_Y > MENU_NODE_Y And MENU_MOUSE_Y < MENU_NODE_Y + 200
				xColor 0, 122, 204, 150

				xRect MENU_NODE_X, MENU_NODE_Y + 100, 201, 50, 1

				If MOUSE_HIT_1
					SKETCH_VIEWMODE = VIEWMODE_GRID
					PROTOTYPE_SELECTED.TYPE_NODE_PROTOTYPE = PROTOTYPE
					NODE_SELECTED = Null
				End If
			End If
			End If

			DrawNode(NodePosX, NodePosY, 200, 100, 100, PROTOTYPE\Inputs, PROTOTYPE\Image)

			xColor 255, 255, 255, 255
			xSetFont FONT_STAND
			xText NodePosX + SKETCH_CENTER_X + 5, NodePosY + SKETCH_CENTER_Y + 112, PROTOTYPE\Discription
		
			i = i + 1
			If i = 4
				i = 0
				j = j + 1
			End If

		Next

		If ((j + 1) * 200) + (WINDOW_HEIGHT - 600) > WINDOW_HEIGHT
			NODE_MENU_SCROLL_CUR = NODE_MENU_SCROLL_CUR + xMouseZSpeed() * 200
			NODE_MENU_SCROLL_CUR = CropValue(NODE_MENU_SCROLL_CUR, -(j * 200 + 200 - WINDOW_HEIGHT + (WINDOW_HEIGHT - 600)), 0)
			NODE_MENU_SCROLL = CurveValue(NODE_MENU_SCROLL_CUR, NODE_MENU_SCROLL, 10)
		End If

		Else
			If PROTOTYPE_SELECTED <> Null
				MENU_MOUSE_X = xMouseX()
				MENU_MOUSE_Y = xMouseY()
				NodePosX = ((MENU_MOUSE_X - SKETCH_CENTER_X) - (MENU_MOUSE_X - SKETCH_CENTER_X) Mod (SKETCH_CELL_SIZE * SKETCH_ZOOM * 0.01)) * 100 / SKETCH_ZOOM
				NodePosY = ((MENU_MOUSE_Y - SKETCH_CENTER_Y) - (MENU_MOUSE_Y - SKETCH_CENTER_Y) Mod (SKETCH_CELL_SIZE * SKETCH_ZOOM * 0.01)) * 100 / SKETCH_ZOOM
				NodeWidth = 201 * SKETCH_ZOOM * 0.01
				NodeHeight = 101 * SKETCH_ZOOM * 0.01
		
				;DrawNode(NodePosX, NodePosY, 200, 100, SKETCH_ZOOM, NODE_SELECTED\Inputs, NODE_SELECTED\Image)

				xResizeImage PROTOTYPE_SELECTED\Image, NodeWidth, NodeHeight
				xDrawImage PROTOTYPE_SELECTED\Image, MENU_MOUSE_X, MENU_MOUSE_Y

				xColor 0, 122, 204, 50
				xRect MENU_MOUSE_X, MENU_MOUSE_Y, NodeWidth, NodeHeight, 1
				xColor 0, 122, 204, 150
				xRect MENU_MOUSE_X, MENU_MOUSE_Y, NodeWidth, NodeHeight, 0

				If MOUSE_HIT_1
					AddNewNode(PROTOTYPE_SELECTED\Name, NodePosX, NodePosY, 200, 100, True)
					PROTOTYPE_SELECTED = Null
				End If
	
			End If
	End If

	i = 0: j = 0
End Function

Function UpdateGrid()

	If SKETCH_VIEWMODE = VIEWMODE_GRID 

		If xMouseDown(2) 
			If xMouseXSpeed() SKETCH_CENTER_AIM_X = SKETCH_CENTER_X - MOUSE_X + xMouseX()
			If xMouseYSpeed() SKETCH_CENTER_AIM_Y = SKETCH_CENTER_Y - MOUSE_Y + xMouseY()
		Else
			MOUSE_X = xMouseX()
			MOUSE_Y = xMouseY() 
		End If
	
		SKETCH_ZOOM_CURRENT = SKETCH_ZOOM_CURRENT + xMouseZSpeed() * 10.0
	End If
	
	If Abs(SKETCH_CENTER_X - SKETCH_CENTER_AIM_X) > 5 SKETCH_CENTER_X = CurveValue(SKETCH_CENTER_AIM_X, SKETCH_CENTER_X, 10) Else MOUSE_X = xMouseX()
	If Abs(SKETCH_CENTER_Y - SKETCH_CENTER_AIM_Y) > 5 SKETCH_CENTER_Y = CurveValue(SKETCH_CENTER_AIM_Y, SKETCH_CENTER_Y, 10) Else MOUSE_Y = xMouseY()

    SKETCH_ZOOM_CURRENT = CropValue(SKETCH_ZOOM_CURRENT, 10, 200)
	SKETCH_ZOOM = CurveValue(SKETCH_ZOOM_CURRENT, SKETCH_ZOOM, 10)

	GridCellSize = SKETCH_CELL_SIZE * SKETCH_ZOOM * 0.01

	GridOffsetX = SKETCH_CENTER_X Mod GridCellSize
	GridOffsetY = SKETCH_CENTER_Y Mod GridCellSize
	GridCenterFactorX = SKETCH_CENTER_X / GridCellSize
	GridCenterFactorY = SKETCH_CENTER_Y / GridCellSize

	For i = 0 To WINDOW_WIDTH / GridCellSize
		
		If SKETCH_ZOOM > 50
			If ((i - GridCenterFactorX) Mod 10) = 0 xColor 255, 255, 255, 15 Else xColor 255, 255, 255, 5
			GridLineX = GridOffsetX + i * GridCellSize
			xLine GridLineX, 0, GridLineX, WINDOW_HEIGHT
		Else
			If ((i - GridCenterFactorX) Mod 20) = 0 xColor 255, 255, 255, 15 Else xColor 255, 255, 255, 5
			If ((i - GridCenterFactorX) Mod 2) = 0
				GridLineX = GridOffsetX + i * GridCellSize
				xLine GridLineX, 0, GridLineX, WINDOW_HEIGHT
			End If
		End If
	Next

	For j = 0 To WINDOW_HEIGHT / GridCellSize
		
		If SKETCH_ZOOM > 50
			If ((j - GridCenterFactorY) Mod 10) = 0 xColor 255, 255, 255, 15 Else xColor 255, 255, 255, 5
			GridLineY = GridOffsetY + j * GridCellSize
			xLine 0, GridLineY, WINDOW_WIDTH, GridLineY
		Else
			If ((j - GridCenterFactorY) Mod 20) = 0 xColor 255, 255, 255, 15 Else xColor 255, 255, 255, 5
			If ((j - GridCenterFactorY) Mod 2) = 0
				GridLineY = GridOffsetY + j * GridCellSize
				xLine 0, GridLineY, WINDOW_WIDTH, GridLineY
			End If
		End If
	Next

	xColor 0, 122, 204, 60

	xLine SKETCH_CENTER_X, 0, SKETCH_CENTER_X, WINDOW_HEIGHT
	xLine 0, SKETCH_CENTER_Y, WINDOW_WIDTH, SKETCH_CENTER_Y
End Function

Function UpdateNodes()
	MENU_MOUSE_X = xMouseX()
	MENU_MOUSE_Y = xMouseY()
	ZOOM# = SKETCH_ZOOM * 0.01

	For NODE.TYPE_NODE = Each TYPE_NODE
		DrawNode(NODE\PosX, NODE\PosY, NODE\Width, NODE\Height, SKETCH_ZOOM, NODE\PROTOTYPE\Inputs, NODE\PROTOTYPE\Image)

		For NODEOTHER.TYPE_NODE = Each TYPE_NODE
			If NODEOTHER <> NODE And NODEOTHER\PosX = NODE\PosX And NODEOTHER\PosY = NODE\PosY
				xColor 204, 0, 0, 50
				xRect SKETCH_CENTER_X + NODE\PosX * ZOOM, SKETCH_CENTER_Y + NODE\PosY * ZOOM, (NODE\Width ) * ZOOM, (NODE\Height) * ZOOM, 1
				xColor 204, 0, 0, 150
				xRect SKETCH_CENTER_X + NODE\PosX * ZOOM, SKETCH_CENTER_Y + NODE\PosY * ZOOM, (NODE\Width ) * ZOOM, (NODE\Height) * ZOOM, 0
				Exit
			End If
		Next
		MousePosX = (MENU_MOUSE_X - SKETCH_CENTER_X) * 100 / SKETCH_ZOOM
		MousePosY = (MENU_MOUSE_Y - SKETCH_CENTER_Y) * 100 / SKETCH_ZOOM

		NODE_X = SKETCH_CENTER_X + NODE\PosX * ZOOM
		NODE_Y = SKETCH_CENTER_Y + NODE\PosY * ZOOM
		NODE_W = (NODE\Width ) * ZOOM
		NODE_H = (NODE\Height) * ZOOM

		NODE_TITLE_X = NODE_X + 10 * ZOOM
		NODE_TITLE_W = NODE\Width * ZOOM - 20 * ZOOM

		If NODE_SELECTED = NODE
			xColor 0, 122, 204, 10
			xRect NODE_TITLE_X, NODE_Y, NODE_TITLE_W, NODE\Height * ZOOM, 1
			xColor 0, 122, 204, 255
			xRect NODE_TITLE_X, NODE_Y, NODE_TITLE_W, NODE\Height * ZOOM, 0

				If NODE_MOVING And SKETCH_VIEWMODE = VIEWMODE_GRID And (Abs(xMouseXSpeed()) > 3 Or Abs(xMouseYSpeed()) > 3)
					NODE\PosX = ((MENU_MOUSE_X - SKETCH_CENTER_X) - (MENU_MOUSE_X - SKETCH_CENTER_X) Mod (SKETCH_CELL_SIZE * SKETCH_ZOOM * 0.01)) * 100 / SKETCH_ZOOM - NODE\Width * 0.5
					NODE\PosY = ((MENU_MOUSE_Y - SKETCH_CENTER_Y) - (MENU_MOUSE_Y - SKETCH_CENTER_Y) Mod (SKETCH_CELL_SIZE * SKETCH_ZOOM * 0.01)) * 100 / SKETCH_ZOOM - NODE\Height * 0.5
				End If 
			
				If xKeyHit(KEY_DELETE)
					DeleteNode(NODE)
					NODE_SELECTED = Null
					UpdateNodes()
					Return False
				End If
		End If

		If NODE\PROTOTYPE\Inputs
			InputHeight = NODE\Height / NODE\PROTOTYPE\Inputs
			For i = 0 To NODE\PROTOTYPE\Inputs - 1
				If NODE\InputSlot[i] 
					xColor 0, 122, 204, 50
					xRect NODE_X, NODE_Y + (i * InputHeight) * ZOOM, 10 * ZOOM, InputHeight *ZOOM, 1
				End If
			Next
		End If

		If NODE\Value
			xColor 0, 122, 204, 50
			xRect NODE_X + (NODE\Width - 10) * ZOOM, NODE_Y, 10 * ZOOM ,NODE_H, 1
		End If

		If MousePosX > NODE\PosX And MousePosX < NODE\PosX + NODE\Width 
		If MousePosY > NODE\PosY And MousePosY < NODE\PosY + NODE\Height
		
			If MousePosX < NODE\PosX + 10

				If NODE\PROTOTYPE\Inputs

					InputHeight = NODE\Height / NODE\PROTOTYPE\Inputs
					InputID = (MousePosY - NODE\PosY) / InputHeight
					
					xColor 0, 122, 204, 100
					xRect NODE_X, NODE_Y + (InputID * InputHeight) * ZOOM, 10 * ZOOM, InputHeight *ZOOM, 1

					xColor 50, 172, 254, 255
					xLine NODE_X + 10 * ZOOM, NODE_Y + (InputID * InputHeight) * ZOOM, NODE_X +10 * ZOOM, NODE_Y + (InputID * InputHeight) * ZOOM + InputHeight * ZOOM

					If MOUSE_HIT_1
					
						If NODE_TRANSMITTER = Null
						
							For NODEOTHER.TYPE_NODE = Each TYPE_NODE
								For i = 0 To NODE_MAX_RECEIVER
									If NODEOTHER\RECEIVER[i] = NODE And NODEOTHER\InputID[i] = InputID + 1

										If NODEOTHER\Value = 1 And GetNodetransmitterCount(NODE, InputID) = 1
											NODE\InputSlot[InputID] = 0
											UpdateNode(NODE)
										End If
										
										NODEOTHER\RECEIVER[i] = Null
										NODEOTHER\InputID[i] = 0
										
										NODE_TRANSMITTER = NODEOTHER
										MOUSE_HIT_1 = 0
										UpdateNodes()
										Return False 
									End If
								Next
							Next
						End If

						If NODE_TRANSMITTER <> Null And NODE_TRANSMITTER <> NODE

							For i = 0 To NODE_MAX_RECEIVER
								If NODE_TRANSMITTER\RECEIVER[i] = NODE And NODE_TRANSMITTER\InputID[i] = InputID + 1  AllreadyConnected = True
							Next

							If Not AllreadyConnected

								For i = 0 To NODE_MAX_RECEIVER
									If NODE_TRANSMITTER\RECEIVER[i] = Null
		
										NODE_TRANSMITTER\InputID[i] = InputID + 1
										NODE_TRANSMITTER\RECEIVER.TYPE_NODE[i] = NODE

										If NODE_TRANSMITTER\Value = 1
											For j = 0 To NODE_MAX_RECEIVER
												If NODE_TRANSMITTER\RECEIVER[j] <> Null NODE_TRANSMITTER\RECEIVER[j]\InputSlot[InputID] = 1
											Next
										End If
		
										UpdateNode(NODE_TRANSMITTER)
		
										For j = 0 To NODE_MAX_RECEIVER
											If NODE_TRANSMITTER\RECEIVER[j] <> Null UpdateNode(NODE_TRANSMITTER\RECEIVER[j])
										Next
										
										NODE_TRANSMITTER = Null
		
										Exit
	
									End If
								Next
							End If
						End If
	
					End If
				End If

			Else If MousePosX < NODE\PosX + NODE\Width - 10
				xColor 0, 122, 204, 100
				xRect NODE_TITLE_X, NODE_Y,NODE_TITLE_W, NODE\Height * ZOOM, 0
	
				If MOUSE_HIT_1
					NODE_SELECTED.TYPE_NODE = NODE
					Nodeselected = True

					Select NODE\PROTOTYPE\Kind
						Case MODE_KIND_BUTTON
							NODE\Value = Not NODE\Value 
					End Select

					UpdateNode(NODE)
					
					For NODEOTHER.TYPE_NODE = Each TYPE_NODE
						For i = 0 To NODE_MAX_RECEIVER
							If NODEOTHER\RECEIVER[i] <> Null UpdateNode(NODEOTHER\RECEIVER[i])
						Next
					Next
				End If

				If MOUSE_DOWN_1 NODE_MOVING = True Else NODE_MOVING =  False
				
			Else
				xColor 0, 122, 204, 100
				xRect NODE_X + (NODE\Width - 10) * ZOOM, NODE_Y, 10 * ZOOM ,NODE_H, 1
				xColor 50, 172, 254, 255
				xLine NODE_X + (NODE\Width - 10) * ZOOM, NODE_Y, NODE_X + (NODE\Width - 10) * ZOOM, NODE_Y + NODE_H

				OnReceiver = True

				If MOUSE_HIT_1
					NODE_TRANSMITTER.TYPE_NODE = NODE
				End If 
			End If
						
		End If
		End If
		
		Select NODE\PROTOTYPE\Kind
			Case MODE_KIND_BUTTON
				If NODE\Value
					xColor 204, 122, 0, 50
					xRect NODE_X + 10 * ZOOM, NODE_Y, NODE_W - 20 * ZOOM, NODE_H, 1
				End If
			Case MODE_KIND_LAMP
				If NODE\Value
					xColor 204, 122, 0, 50
					xRect NODE_X + 10 * ZOOM, NODE_Y, NODE_W - 20 * ZOOM, NODE_H, 1
				End If
		End Select
	Next
	
    If NODE_TRANSMITTER <> Null
		xColor 128, 128, 128, 255

		OutputX = SKETCH_CENTER_X + (NODE_TRANSMITTER\PosX + NODE_TRANSMITTER\Width - 3) * ZOOM
		OutputY = SKETCH_CENTER_Y + (NODE_TRANSMITTER\PosY + NODE_TRANSMITTER\Height * 0.5) * ZOOM
		xLine OutputX, OutputY, MENU_MOUSE_X, MENU_MOUSE_Y

		If OnReceiver = False And MOUSE_HIT_1 NODE_TRANSMITTER = Null
	End If

	For NODE.TYPE_NODE = Each TYPE_NODE
		For i = 0 To NODE_MAX_RECEIVER
			If NODE\RECEIVER[i] <> Null

			
				If NODE_BLOCK_CONNECTIONS
					If NODE\Value
						xColor 50, 172, 254, 255
					Else
						xColor 200, 200, 200, 100
					End If
				
					OutputX = SKETCH_CENTER_X + (NODE\PosX + NODE\Width) * ZOOM
					OutputY = SKETCH_CENTER_Y + (NODE\PosY + NODE\Height * 0.5) * ZOOM
					InputX =  OutputX + ((NODE\RECEIVER[i]\PosX - (NODE\PosX + NODE\Width) ) * 0.5) * ZOOM
					InputY =  OutputY
					xLine OutputX, OutputY, InputX, InputY  
				
					OutputX = InputX
					OutputY = InputY
		
					InputX =  InputX
					InputY =  SKETCH_CENTER_Y + (NODE\RECEIVER[i]\PosY + (NODE\RECEIVER[i]\Height / NODE\RECEIVER[i]\PROTOTYPE\Inputs) * NODE\InputID[i] - (NODE\RECEIVER[i]\Height / NODE\RECEIVER[i]\PROTOTYPE\Inputs) * 0.5) * ZOOM
					xLine OutputX, OutputY, InputX, InputY  
		
					OutputX = InputX
					OutputY = InputY
					InputX =  SKETCH_CENTER_X + (NODE\RECEIVER[i]\PosX) * ZOOM
					InputY =  SKETCH_CENTER_Y + (NODE\RECEIVER[i]\PosY + (NODE\RECEIVER[i]\Height / NODE\RECEIVER[i]\PROTOTYPE\Inputs) * NODE\InputID[i] - (NODE\RECEIVER[i]\Height / NODE\RECEIVER[i]\PROTOTYPE\Inputs) * 0.5) * ZOOM
					xLine OutputX, OutputY, InputX, InputY

				Else
					If NODE\Value
						xColor 50, 172, 254, 255
					Else
						xColor 128, 128, 128, 255
					End If
		
					OutputX = SKETCH_CENTER_X + (NODE\PosX + NODE\Width - 3) * ZOOM
					OutputY = SKETCH_CENTER_Y + (NODE\PosY + NODE\Height * 0.5) * ZOOM
		
					InputX =  SKETCH_CENTER_X + (NODE\RECEIVER[i]\PosX + 3) * ZOOM
					InputY =  SKETCH_CENTER_Y + (NODE\RECEIVER[i]\PosY + (NODE\RECEIVER[i]\Height / NODE\RECEIVER[i]\PROTOTYPE\Inputs) * NODE\InputID[i] - (NODE\RECEIVER[i]\Height / NODE\RECEIVER[i]\PROTOTYPE\Inputs) * 0.5) * ZOOM
		
					xLine OutputX, OutputY, InputX, InputY  
				End If
			End If
		Next
	Next


	If Nodeselected = False And MOUSE_HIT_1
		NODE_SELECTED = Null
	End If

	If xKeyHit(KEY_1) NODE_BLOCK_CONNECTIONS = Not NODE_BLOCK_CONNECTIONS
End Function 

Function UpdateNode(NODE.TYPE_NODE)
	Local LastValue = NODE\Value

	Select NODE\PROTOTYPE\Kind
	
		Case NODE_KIND_USER
			Select NODE\PROTOTYPE\Inputs
				Case 1				
					If NODE\InputSlot[0] = 0 NODE\Value = NODE\PROTOTYPE\Result[0]
					If NODE\InputSlot[0] = 1 NODE\Value = NODE\PROTOTYPE\Result[1]
				Case 2
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 0 NODE\Value = NODE\PROTOTYPE\Result[0]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 1 NODE\Value = NODE\PROTOTYPE\Result[1]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 0 NODE\Value = NODE\PROTOTYPE\Result[2]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 1 NODE\Value = NODE\PROTOTYPE\Result[3]
				Case 3
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 0 NODE\Value = NODE\PROTOTYPE\Result[0]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 1 NODE\Value = NODE\PROTOTYPE\Result[1]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 0 NODE\Value = NODE\PROTOTYPE\Result[2]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 1 NODE\Value = NODE\PROTOTYPE\Result[3]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 0 NODE\Value = NODE\PROTOTYPE\Result[4]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 1 NODE\Value = NODE\PROTOTYPE\Result[5]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 0 NODE\Value = NODE\PROTOTYPE\Result[6]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 1 NODE\Value = NODE\PROTOTYPE\Result[7]
				Case 4
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 0 And NODE\InputSlot[3] = 0 NODE\Value = NODE\PROTOTYPE\Result[0]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 0 And NODE\InputSlot[3] = 1 NODE\Value = NODE\PROTOTYPE\Result[1]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 1 And NODE\InputSlot[3] = 0 NODE\Value = NODE\PROTOTYPE\Result[2]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 1 And NODE\InputSlot[3] = 1 NODE\Value = NODE\PROTOTYPE\Result[3]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 0 And NODE\InputSlot[3] = 0 NODE\Value = NODE\PROTOTYPE\Result[4]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 0 And NODE\InputSlot[3] = 1 NODE\Value = NODE\PROTOTYPE\Result[5]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 1 And NODE\InputSlot[3] = 0 NODE\Value = NODE\PROTOTYPE\Result[6]
					If NODE\InputSlot[0] = 0 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 1 And NODE\InputSlot[3] = 1 NODE\Value = NODE\PROTOTYPE\Result[7]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 0 And NODE\InputSlot[3] = 0 NODE\Value = NODE\PROTOTYPE\Result[8]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 0 And NODE\InputSlot[3] = 1 NODE\Value = NODE\PROTOTYPE\Result[9]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 1 And NODE\InputSlot[3] = 0 NODE\Value = NODE\PROTOTYPE\Result[10]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 0 And NODE\InputSlot[2] = 1 And NODE\InputSlot[3] = 1 NODE\Value = NODE\PROTOTYPE\Result[11]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 0 And NODE\InputSlot[3] = 0 NODE\Value = NODE\PROTOTYPE\Result[12]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 0 And NODE\InputSlot[3] = 1 NODE\Value = NODE\PROTOTYPE\Result[13]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 1 And NODE\InputSlot[3] = 0 NODE\Value = NODE\PROTOTYPE\Result[14]
					If NODE\InputSlot[0] = 1 And NODE\InputSlot[1] = 1 And NODE\InputSlot[2] = 1 And NODE\InputSlot[3] = 1 NODE\Value = NODE\PROTOTYPE\Result[15]
				
			End Select
			
			If LastValue <> NODE\Value 
				NodeValve(NODE)
			End If
		
		Case MODE_KIND_BUTTON
			;If LastValue <> NODE\Value 
				NodeValve(NODE)
			;End If
		
		
		Case MODE_KIND_LAMP

			If NODE\InputSlot[0] = True NODE\Value = True Else NODE\Value = False

			If LastValue <> NODE\Value 
				NodeValve(NODE)
			End If
	End Select
End Function

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ NODES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Function AddNewNodePrototype(Name$, Discription$, Result$, Inputs, Kind = NODE_KIND_USER)
	PROTOTYPE.TYPE_NODE_PROTOTYPE = New TYPE_NODE_PROTOTYPE

	PROTOTYPE\Name = Name
	PROTOTYPE\Discription = Discription

	PROTOTYPE\Inputs = Inputs

	Result2$ = ""

	For i = 0 To Len(Result) - 1
		PROTOTYPE\Result[i] = Int(Mid(Result, i + 1, 1))

		Result2 = Result2 + Str(PROTOTYPE\Result[i])
		
	Next

	PROTOTYPE\Kind = Kind

	PROTOTYPE\Image = xCreateImage(200, 100)

	xSetBuffer xImageBuffer(PROTOTYPE\Image)
	
		xCls
	
		xSetFont FONT_LARGE
		xText (200 - xStringWidth(PROTOTYPE\Name)) * 0.5, 28, PROTOTYPE\Name
	xSetBuffer xBackBuffer()
End Function

Function AddNewNode(PrototypeName$, PosX, PosY, Width, Height, Selected = False)

	For PROTOTYPE.TYPE_NODE_PROTOTYPE = Each TYPE_NODE_PROTOTYPE
		If Lower(PrototypeName) = Lower(PROTOTYPE\Name)
			NODE.TYPE_NODE = New TYPE_NODE
		
			NODE\PROTOTYPE.TYPE_NODE_PROTOTYPE = PROTOTYPE
			NODE\PosY = PosY
			NODE\PosX = PosX
		
			NODE\Width  = Width
			NODE\Height = Height

			If Selected	NODE_SELECTED = NODE

			UpdateNode(NODE)
		End If
	Next
End Function

Function DeleteNode(NODE.TYPE_NODE)

	If NODE_TRANSMITTER = NODE NODE_TRANSMITTER = Null

	For NODEOTHER.TYPE_NODE = Each TYPE_NODE
		For i = 0 To NODE_MAX_RECEIVER
			If NODEOTHER <> NODE And NODEOTHER\RECEIVER[i] = NODE
				NODEOTHER\RECEIVER[i] = Null
				NODEOTHER\InputID[i] = 0
			End If
		Next
	Next

	For i = 0 To NODE_MAX_RECEIVER
		If NODE\RECEIVER[i] <> Null
			NODE\RECEIVER[i]\InputSlot[NODE\InputID[i] - 1] = 0
		End If
	Next

	Delete NODE
	
	For NODEOTHER.TYPE_NODE = Each TYPE_NODE
		UpdateNode(NODEOTHER)
	Next
End Function

Function ChekNodeCicle.TYPE_NODE(CHECKNODE.TYPE_NODE, NODE.TYPE_NODE)
	For i = 0 To NODE_MAX_RECEIVER
		If CHECKNODE\RECEIVER[i] <> Null
			If CHECKNODE\RECEIVER[i] = NODE Return NODE Else Return ChekNodeCicle(CHECKNODE\RECEIVER[i], NODE) 
		End If
	Next

	Return Null 
End Function

Function GetNodetransmitterCount(CHECKNODE.TYPE_NODE, InputID)
	For NODE.TYPE_NODE = Each TYPE_NODE
		For i = 0 To NODE_MAX_RECEIVER
			If NODE\RECEIVER[i] = CHECKNODE And NODE\InputID[i] = InputID + 1 NodesCount = NodesCount + 1
		Next
	Next
	Return NodesCount
End Function
 
Function GetNodetransmitterSignalCount(CHECKNODE.TYPE_NODE, InputID)
	For NODE.TYPE_NODE = Each TYPE_NODE
		For i = 0 To NODE_MAX_RECEIVER
			If NODE\RECEIVER[i] = CHECKNODE And NODE\InputID[i] = InputID + 1 And NODE\Value = 1 NodesCount = NodesCount + 1
		Next
	Next
	Return NodesCount
End Function

Function NodeValve(NODE.TYPE_NODE)

	For i = 0 To NODE_MAX_RECEIVER
		If NODE\RECEIVER[i] <> Null
			
			If NODE\Value = 1
				NODE\RECEIVER[i]\InputSlot[NODE\InputID[i] - 1] = NODE\Value
				UpdateNode(NODE\RECEIVER[i])
			ElseIf NODE\Value = 0			
				If GetNodetransmitterCount(NODE\RECEIVER[i], NODE\InputID[i] - 1) = 1
					NODE\RECEIVER[i]\InputSlot[NODE\InputID[i] - 1] = NODE\Value
					UpdateNode(NODE\RECEIVER[i])
				Else
					If GetNodetransmitterSignalCount(NODE\RECEIVER[i], NODE\InputID[i] - 1) = 0
						NODE\RECEIVER[i]\InputSlot[NODE\InputID[i] - 1] = 0
						UpdateNode(NODE\RECEIVER[i])
					Else
						UpdateNode(NODE\RECEIVER[i])
					End If
				End If
			End If
			
		End If
	Next
	
End Function

Function DrawNode(PosX, PosY, Width, Height, ZoomFactor, Inputs, Image)
		ZOOM# = ZoomFactor * 0.01
		NODE_X = SKETCH_CENTER_X + PosX * ZOOM
		NODE_Y = SKETCH_CENTER_Y + PosY * ZOOM
		NODE_W = Width * ZOOM + 1
		NODE_H = Height * ZOOM + 1
		NODE_CELL_SIZE = 6 * ZOOM

		xColor 64, 64, 64, 255
		xRect NODE_X, NODE_Y, NODE_W, NODE_H, 1

		xColor 48, 48, 48, 255
		xRect NODE_X + 10 * ZOOM, NODE_Y, NODE_W - 20 * ZOOM, NODE_H, 1

		xColor 76, 76, 76, 255
		xRect NODE_X + 10 * ZOOM, NODE_Y, NODE_W - 20 * ZOOM, NODE_H, 0

		xColor 32, 32, 32, 255
		xRect NODE_X + NODE_W - NODE_CELL_SIZE, NODE_Y + (NODE_H - NODE_CELL_SIZE) * 0.5, NODE_CELL_SIZE, NODE_CELL_SIZE, 1

		If Inputs
		
			For i = 1 To Inputs
				FactorY = NODE_Y + NODE_H / Inputs * i
				xRect NODE_X, FactorY - (NODE_H / Inputs + NODE_CELL_SIZE) * 0.5, NODE_CELL_SIZE, NODE_CELL_SIZE, 1
				If i < Inputs
					xLine NODE_X, FactorY, NODE_X + 9 * SKETCH_ZOOM * 0.01, FactorY
				End If
			Next
		End If
		
		xResizeImage Image, NODE_W, NODE_H
		xDrawImage Image, NODE_X, NODE_Y
End Function

;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TOOLS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Function CurveValue#(newvalue#, oldvalue#, increments)
	If increments >  1 Then oldvalue# = oldvalue# - (oldvalue# - newvalue#) / increments 
	If increments <= 1 Then oldvalue# = newvalue# 
	Return oldvalue# 
End Function

Function CropValue(Value, DiapStart, DiapEnd)
	If Value < DiapStart Return DiapStart
	If Value > DiapEnd Return DiapEnd
	Return Value
End Function

Function StrCut$(InputString$, WordNum, Seperators$ = " ")

        FoundWord  = False
        WordsFound = 0

        For CharLoop = 1 To Len(InputString$)
        	ThisChar$ = Mid$(InputString$, CharLoop, 1)
            If Instr(Seperators$, ThisChar$, 1)
            	If FoundWord
                  	WordsFound = WordsFound + 1
                        If WordsFound = WordNum
                        	Return Word$
                        Else
					Word$ = ""
					FoundWord = False
                        End If
                  End If                                                
            Else
            	FoundWord = True
                Word$ = Word$ + ThisChar$                     
            End If
        Next    

        If (WordsFound + 1) = WordNum Return Word$ Else Return ""
End Function
