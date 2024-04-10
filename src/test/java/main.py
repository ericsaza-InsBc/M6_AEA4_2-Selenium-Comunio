import requests
import os
from datetime import date
from bs4 import BeautifulSoup

# Lee el archivo 'links.txt' y almacena los datos en una lista de tuplas (nombre, id)
with open('./src/test/java/links.txt', 'r') as file:
    lines = file.readlines()
    player_data = [line.strip().split(' - ') for line in lines]

# Día actual
today = date.today().strftime('%Y-%m-%d')

with open("./src/test/java/players_data/" + today + ".txt", "w") as file:
    # Itera sobre los datos de los jugadores
    for nombre, id in player_data:

        # Construye la URL con el nombre e ID del jugador
        url = f'https://www.comuniate.com/jugadores/{id}/{nombre}'

        # Realiza una solicitud HTTP GET a la URL de los jugadores
        response = requests.get(url)

        # Verifica si la solicitud fue exitosa (código de estado 200)
        if response.status_code == 200:

            # Parsea el contenido HTML de la página
            soup = BeautifulSoup(response.content, 'lxml')

            # Encuentra el elemento que indica si el jugador es titular o no (ajusta esto según la estructura del sitio web)
            titular_element = soup.find('div', {'class': 'col-xs-2 label-success'})
            mediaDivElement = soup.find_all("div", {"class" : "col-xs-3 cuadro"})
            media = mediaDivElement[1].find("strong").get_text()

            if titular_element:
                file.write(f'{nombre} / {media} / SI')
            else:
                file.write(f'{nombre} / {media} / NO')
            
            # Comprabamos si el jugador esta lesionado
            lesionado = soup.find("div", {"style" : "background-color: #ff3232;color: #fff;padding: 3px; font-size: 12px; border-radius: 10px;"})
            if lesionado:
                file.write(" / LESIONADO")
            else:
                file.write(" / NO LESIONADO")
                
            # Construye la URL con el nombre e ID del jugador para el mercado
            url = f'https://www.comuniate.com/mercado/{id}/{nombre}'

            # Realiza una solicitud HTTP GET a la URL del mercado de los jugadores
            response = requests.get(url)

            # Verifica si la solicitud fue exitosa (código de estado 200)
            if response.status_code == 200:

                # Parsea el contenido HTML de la página
                soup = BeautifulSoup(response.content, 'lxml')

                # Encuentra el elemento que contiene los precios (ajusta esto según la estructura del sitio web)
                divPrices = soup.find_all('table', {'class': 'table'})
                tablePrices = divPrices[1]
                spans = tablePrices.find_all('span')

                file.write(' /')
                for i, span in enumerate(spans):
                    price = span.text.replace(" ", "").replace("€", "")
                    file.write(" " + price)
                    if i < len(spans) - 1:  # Agrega ">" solo si no es el último elemento
                        file.write(" >")
                file.write('\n')

            else:
                print(f'No se pudo acceder a la página de {nombre} ({id}). Código de estado: {response.status_code}')

        else:
            print(f'No se pudo acceder a la página de {nombre} ({id}). Código de estado: {response.status_code}')
