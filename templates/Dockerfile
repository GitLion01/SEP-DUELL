FROM node:17-alpine

#subfolder
WORKDIR /app

#copy the package.json file into the image ,and with (.) copy it to the folder app
COPY package.json .

RUN npm install

#first . means copy every thing where the application is and put them in the app directory
COPY . .

#port
EXPOSE 4000

CMD ["npm", "start"]
