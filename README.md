# FOR DEVELOPMENT
- Development in separate IDEs, I used VSCODE for frontend and Eclipse for backend
- You may have to install separate Lombok plugin into Eclipse IDE
- In the output directory you can see the structure of concrete result

## SETTING UP OUTPUT DIRECTORY
1. mkdir -p ~/cellApp 
2. cp -rf ./cellApp/* ~/cellApp/
3. cd ~/cellApp/wound-lg/code/
4. python3 -m venv venv
5. source ./venv/bin/activate
6. pip3 install -r requirements.txt 

## FRONTEND
1. cd ./frontend/cell-front-end/cell-migration-front
2. npm install
3. npm run serve

## BACKEND
1. cd ./backend/cell-migration-backCOMPRESSED
2. mvn install
3. mvn test

# FULL APP BUILD
1. cd ./frontend/cell-front-end/cell-migration-front
2. npm install
3. npm run build
4. cp -r ./dist/* ./backend/cell-migration-backCOMPRESSED/src/main/resources/static/
5. mvn install
6. mvn test
7. frontend will be server at root URL .../


