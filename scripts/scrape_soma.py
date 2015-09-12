from bs4 import BeautifulSoup
import requests
import json

def scrape_stations():
    r = requests.get("http://somafm.com/listen/")
    assert r.status_code == 200

    soup = BeautifulSoup(r.text, 'html.parser')

    for station in soup.find(id="stations").find_all("li"):
        name = station.find("h3").text
        desc = station.find(class_="descr").text
        playlists = sorted([stream.get("href") for stream in station.find_all("a") if stream.get("href", "").endswith(".pls")])
        icon_url = "http://somafm.com" + station.find("img")["src"]
        station_url = "http://somafm.com" + station.find(class_="playing").find("a")["href"][:-len("songhistory.html")]

        yield dict(network="SomaFM",
                   stationUrl=station_url,
                   name=name,
                   description=desc,
                   playlists=playlists,
                   iconUrl=icon_url,
                   commercials=False)


def main():
    stations = list(scrape_stations())
    print json.dumps(stations, sort_keys=True, indent=4)

if __name__ == "__main__":
    main()