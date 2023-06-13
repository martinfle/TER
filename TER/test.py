from pycsp3 import *
import time
import signal 
import multiprocessing as mp

def parse_sudoku(file_path) :    
    with open(file_path, 'r') as file:   
        lines = file.readlines()
        n = int(lines[0])
        sudoku = []     
        for line in lines[1:]:
            row = []
            for char in line.strip():
                if char.isdigit():
                    row.append(int(char))                
            sudoku.extend(row)
    return n,sudoku

def trouverSolution (n,sudoku) :
    clear()    
    
    resultat = VarArray(size = [n*n,n*n], dom = range(1,n*n+1))   
    
      
    start = time.time()
    satisfy (
        AllDifferent(resultat, matrix = True),
    
        [AllDifferent(resultat[i:i+n, j:j+n]) for i in range(0, n*n-n+1, n) for j in range(0,n*n-n+1,n)],

        [resultat[i][j] == sudoku[i*n*n+j] for i in range (n*n) for j in range(n*n) if sudoku and sudoku[i*n*n+j] != 0]
    ) 
    
    if solve () is SAT:
        end = time.time() - start       

sudoku_file = "TER/Sudoku/2_3_0"
n, sudoku = parse_sudoku(sudoku_file)
trouverSolution(n, sudoku)