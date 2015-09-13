import json
import requests

from lxml import etree


def scrape_station(station_id):
    url = "http://api2.iheart.com/api/v2/content/liveStations/" + station_id
    response = requests.get(url)
    return response.json() if response.status_code == 200 else None


def scrape_stations():
    response = requests.get("http://www.iheartradio.com/cc-common/iphone/station_list.xml")
    root = etree.fromstring(response.content)
    station_ids = root.xpath("//station_id")
    for station_id in station_ids:

        station = scrape_station(station_id.text)
        if not station:
            continue

        name = station["hits"][0]["name"]
        description = station["hits"][0]["description"]
        stream_url = station["hits"][0]["streams"].get("hls_stream")
        icon_url = station["hits"][0]["logo"]

        if not stream_url:
            continue

        yield dict(network="iHeartRadio",
                   stationUrl="http://www.iheartradio.com",
                   name=name,
                   description=description,
                   hlsStreams=[stream_url],
                   iconUrl=icon_url,
                   commercials=True)


def main():
    stations = list(scrape_stations())
    print json.dumps(stations, sort_keys=True, indent=4)

if __name__ == "__main__":
    main()
