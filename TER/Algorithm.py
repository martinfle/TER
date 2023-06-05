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

def creat_graph (n) :
    graph = {}
    for i in range (n*n*n*n):
        graph[i] = set()
    for i in range (n*n) :
        for j in range (n*n):
            vertex = i*n*n +j
            for k in range(n*n):
                neighbor = i*n*n + k
                if neighbor != vertex :
                    graph[vertex].add(neighbor)

                neighbor = k*n*n + j
                if neighbor != vertex:
                    graph[vertex].add(neighbor)
            block_row = i //n
            block_col = j //n
            for row in range(block_row * n, (block_row+1)*n) :
                for col in range(block_col*n, (block_col+1)*n) :
                    neighbor = row*n*n+col
                    if (neighbor != vertex) :
                        graph[vertex].add(neighbor)
    
    return graph

def trouverSolution (n , sudoku, graph, queue) :
    clear()    
    
    resultat = VarArray(size = n*n*n*n, dom = range(1,n*n+1))   

      
    start = time.time()
    for i in range (len(sudoku)) :
        for j in graph[i] :        
            satisfy (
                resultat[i] != resultat [j]
            )

    for i in range(len(sudoku)) :
        if sudoku[i] != 0 :
            satisfy (
                resultat[i] == sudoku[i]
            )          
        

    if solve () is SAT:
        end = time.time() - start
        print(values(resultat))
        queue.put(end)    
    
def run_code (n, sudoku, graph, queue) :
    try :
        trouverSolution(n, sudoku, graph, queue)
    except:
        pass

fichier = open("resultat_pycsp3","w")
for i in range (4,10) :   
    for j in range (3,9) : 
        result = []
        for l in range (2) :
            result.append(0)             
        for k in range (0,100) :
            queue = mp.Queue()
            sudoku_file = "TER/Sudoku/"+str(i)+"_"+str(j)+"_"+str(k)
            n, sudoku = parse_sudoku(sudoku_file)
            graph = creat_graph(4)
            process = mp.Process(target = run_code, args=(n, sudoku, graph, queue))
            process.start()
            time.sleep(60)

            if process.is_alive() :
                process.terminate()
                process.join()
                print("c'est raté")
            else :
                x = queue.get()
                result[0] += x
                result[1] += 1
                print("bravo")
        if result[1] == 0:
            result[1] = 1
        fichier.write("sudoku de type "+ str(i) + " " + str(j) + " temps d'execution moyen: " + str(result[0]/result[1]) + " nombre de réussite : " + str(result[1]) + "/100\n")
        print("sudoku de type " + str(i) + " " + str(j) + " temps d'execution moyen: " + str(result[0]/result[1]) + " nombre de réussite : " + str(result[1]) + "/100")
fichier.close()
           

            