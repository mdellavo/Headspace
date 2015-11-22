import requests
import json

API_KEY = "MDExODkwMDg3MDEzNzU0NTY2ODlmYWY0Yw001"

org_ids = [1, 4788725, 20, 936, 471, 172, 1150, 79, 96, 756, 148, 150, 252, 1038, 665, 111, 1146, 156, 134, 4787204,
           231, 140, 314, 360, 393, 395, 704, 552, 715]

station_ids = ["WBHM", "WRWA", "WSGN", "WJAB", "WLRH", "WLJS-FM", "WHIL-FM", "WVAS", "WTSU", "WQPR", "WAPR", "WUAL-FM",
               "KSKA", "KNBA", "KBRW", "KBRW-FM", "KYUK", "KCUK", "KDLG", "KUAC", "KIYU", "KXGA", "KHNS", "KBBI",
               "KRNN", "KTOO", "KXLL", "KDLL", "KRBD", "KMXT", "KOTZ", "KXKM", "KSKO", "KFSK", "KSDP", "KCAW",
               "KUHB-FM", "KTNA", "KUCB", "KCHU", "KSTK", "KPUB", "KNAU", "KNAG", "KUYI", "KNAD", "KBAQ", "KJZZ",
               "KNAQ", "KNAA", "KUAT-FM", "KUAZ", "KUAZ-FM", "KNNB", "KAWC", "KAWC-FM", "KBSA", "KUAF", "KASU",
               "KLRE-FM", "KUAR", "KDFC", "KHSU", "KPRX", "KNHM", "KNCA", "KQVO", "KCHO", "KVLA-FM", "KHSR", "KVPR",
               "KXSR", "KCRI", "KPCC", "KCRW", "KUSC", "KLDD", "KPMO", "KCRY", "KESC", "KMJC", "KNSQ", "KQEI-FM",
               "KCSN", "KCRU", "KPSC", "KPCC", "KZYX", "KQNC", "KFPR", "KUOR-FM", "KNHT", "KUOP", "KXJZ", "KXPR",
               "KNBX", "KVCR", "KPBS-FM", "KSDS", "KALW", "KQED-FM", "KUSF", "KCBX", "KCSM", "KDRW", "KCLU", "KSBX",
               "KUSP", "KCRW", "KRCB-FM", "KAZU", "KJPR", "KXJS", "KKTO", "KCLU-FM", "KDSC", "KZYZ", "KNYR", "KSYC",
               "KRZA", "KAJX", "KCFC", "KCJX", "KDNK", "KVOV", "KRCC", "KSJD", "KPYR", "KBUT", "KPRU", "KCFR-FM",
               "KDCO", "KUVO", "KVOD", "KPRN", "KUNC", "KSUT", "KUTE", "KECC", "KCME", "KPRH", "KVMT", "KVNF", "KCFP",
               "KKPC", "KRNC", "KOTO", "KPRE", "WSHU-FM", "WVOF", "WNPR", "WESU", "WPKT", "WHDD", "WHDD-FM", "WQQQ",
               "WEDW-FM", "WSHU", "WECS", "WRTX", "WDDE", "WMPH", "WAMU", "WETA", "WGCU-FM", "WQCS", "WUFT-FM", "WJUF",
               "WJCT-FM", "WKWM", "WMKO", "WFIT", "WDNA", "WLRN-FM", "WMFE-FM", "WUCF-FM", "WFSW", "WKGC", "WKGC-FM",
               "WUWF", "WSMR", "WFSQ", "WFSU-FM", "WMNF", "WUSF", "WUNV", "WUGA", "WABE", "WCLK", "WACG-FM", "WWIO-FM",
               "WUWG", "WNGH-FM", "WTJB", "WNGU", "WPPR", "WATY", "WJWV", "WKSU", "WGPB", "WSVH", "WFSL", "WABR",
               "WWET", "WJSP-FM", "WXVS", "KPRG", "KANO", "KHPR", "KIPO", "KIPM", "KKUA", "KBSU-FM", "KBSX", "KIBX",
               "KBSY", "KNWO", "KBSK", "KBSM", "KBSQ", "KRFA-FM", "KISU-FM", "KBYI", "KBSS", "KWRV", "KBSW", "KEZJ",
               "WSIU", "WBEZ", "WNIJ", "WSIE", "WEPS", "WNIE", "WVKC", "WDCB", "WKCC", "WNIW", "WIUM", "WBEQ", "WVSI",
               "WGLT", "WUSI", "WCBU", "WIPA", "WQUB", "WVIK", "WNIJ", "WUIS", "WNIQ", "WILL", "WILL-FM", "WIUW",
               "WBSB", "WFIU", "WBEW", "WNDY", "WVPE", "WNIN-FM", "WBOI-FM", "WFCI", "WGVE-FM", "WBSH", "WFYI-FM",
               "WLPR-FM", "WBSW", "WBST", "WBKE-FM", "WBSJ", "WVUB", "WBAA", "WBAA-FM", "WOI", "WOI-FM", "KNSB", "KNSC",
               "KHKE", "KUNI", "KCCK-FM", "KIWR", "KLCD", "KLNI", "KNSY", "KNSK", "KSUI", "WSUI", "KNSL", "KRNI",
               "KNSM", "KICJ", "KOJI", "KNSZ", "KICW", "KICP", "KICL", "KWIT", "KBBG", "KANQ", "KANH", "KANZ", "KHCT",
               "KZAN", "KZNA", "KHCC-FM", "KANU", "KANV", "KRPS", "KHCD", "KMUW", "WKYU-FM", "WEKF", "WKUE", "WEKH",
               "WKPB", "WNKU", "WUKY", "WFPK", "WFPL", "WUOL-FM", "WMKY", "WKMS-FM", "WEKU", "WDCL", "KLSA", "WBRH",
               "WRKF", "KSLU", "KRVS", "KEDM", "WWNO", "KDAQ", "KTLN", "WMEH", "WMED", "WMEP", "WMEF", "WMEA", "WMEM",
               "WMEW", "KRNM", "WEAA", "WYPR", "WYPF", "WFWM", "WGMS", "WRAU", "WSDL", "WYPO", "WESM", "WSCL", "WTMD",
               "WKHS", "WFCR", "WBUR-FM", "WGBH", "WUMB-FM", "WZAI", "WNNZ-FM", "WFPB-FM", "WAMQ", "WCCT-FM", "WCRB",
               "WNAN", "WNCK", "WNEF", "WFPB", "WSDH", "WBSL-FM", "WAIC", "WUMG", "WBAS", "WNNZ", "WCAI", "WBPR",
               "WICN", "WGVU-FM", "WCML-FM", "WUOM", "WUCX-FM", "WAUS", "WDET-FM", "WICV", "WKAR", "WKAR-FM", "WFUM",
               "WBLU-FM", "WVGR", "WCMW-FM", "WHBP", "WGGL-FM", "WIAA", "WMUK", "WGVU", "WIAB", "WLMN", "WNMU-FM",
               "WCMU-FM", "WGVS", "WCMB-FM", "WCMZ-FM", "WWCM", "WICA", "WBLV", "WGVS-FM", "WEMU", "KNCM", "KRSU",
               "KNSE", "KCRB-FM", "KNBJ", "KBPN", "KBPR", "WIRN", "KNSR", "KSJR-FM", "WSCN", "KUMD-FM", "WSCD-FM",
               "KCMF", "KNWF", "WLSN", "WMLS", "WTIP", "WGPO", "KAXE", "WGRH", "KITF", "KXLC", "KMSU", "KBEM-FM",
               "KFAI", "KCCD", "KCCM", "KCMP", "KRFI", "KLSE", "KMSE", "KZSE", "KRXW", "KNOW-FM", "KSJN", "KGAC",
               "KNGA", "KNTN", "KQMN", "WIRR", "KNSW", "KRSW", "WMAH-FM", "WMAE-FM", "WMAU-FM", "WMAO-FM", "WURC",
               "WJSU-FM", "WMPN-FM", "WPRL", "WMAW-FM", "WMAB-FM", "WMAV-FM", "WMSV", "KSMS-FM", "KRCU", "KRNW", "KBIA",
               "KOPN", "KCUR-FM", "KXCV", "KMST", "KSMU", "KWMU", "KSEF", "KTBG", "KSMW", "KEMC", "KBMC", "KGLT",
               "KAPC", "KGVA", "KGPR", "KUFN", "KUHM", "KUKL", "KYPR", "KUFM", "KTNE-FM", "KMNE-FM", "KCNE-FM",
               "KHNE-FM", "KLNE-FM", "KUCV", "KZUM", "KRNE-FM", "KXNE-FM", "KPNE-FM", "KIOS-FM", "KNCC", "KBSJ", "KCNV",
               "KNPR", "KUNV", "KCEP", "KWPR", "KLNR", "KUNR", "KTPH-FM", "WEVO", "WEVC", "WEVH", "WEVJ", "WEVN",
               "WEVS", "WNJN-FM", "WNJS-FM", "WNJB-FM", "WNJZ", "WNTI", "WBJB-FM", "WNJM", "WBGO", "WRTQ", "WNJP",
               "WNJT-FM", "KANW", "KUNM", "KCIE-FM", "KGLP", "KRWG", "KOAZ", "KMTH-FM", "KENW-FM", "KTDB", "KNLK",
               "WAMC", "WAMC-FM", "WSKG-FM", "WSQX-FM", "WXLH", "WXLB", "WNYE", "WCWP", "WBFO", "WNED-FM", "WCAN",
               "WSLU", "WSLZ", "WSQE", "WCVF-FM", "WEOS", "WXLB", "WRCU-FM", "WSQA", "WXXY", "WSQG-FM", "WUBJ", "WNJA",
               "WJFF", "WAMK", "WXLL", "WSLO", "WOSR", "WFUV", "WNYC-AM", "WNYC-FM", "WXLG", "WOLN", "WSQC-FM", "WRVO",
               "WRVD", "WXLU", "WCEL", "WRHV", "WXXI", "WXXI-FM", "WRUR-FM", "WSLL", "WMHT-FM", "WSUF", "WPPB",
               "WRLI-FM", "WAER", "WCNY-FM", "WANC", "WXLS", "WRVN", "WUNY", "WJNY", "WRVJ", "WSLJ", "WCQS", "WBJD",
               "WBUX", "WUNC", "WFAE", "WDAV", "WNCU", "WRVS-FM", "WFSS", "WFQS", "WFHE", "WKNS", "WUND-FM", "WURI",
               "WTEB", "WZNB", "WSHA", "WCPE", "WRQM", "WNCW", "WHQR", "WFDD", "WSNC", "KEYA", "KCND", "KDPR", "KDSU",
               "KUND-FM", "KFJM", "KPRJ", "KMPR", "KPPR", "WOUB", "WOUB-FM", "WGBE", "WOUC-FM", "WOUH-FM", "WGUC",
               "WVXU", "WCPN", "WCBE", "WVSG", "WOSU-FM", "WOSE", "WDPR", "WGDE", "WOUL-FM", "WKSU-FM", "WGLE", "WOSV",
               "WOSB", "WNRK", "WKRJ", "WMUB", "WOSP", "WKSV", "WGTE-FM", "WCSU-FM", "WKRW", "WYSO", "WYSU", "WOUZ-FM",
               "KOUA", "KOCU", "KLCU", "KYCU", "KOSN", "KCCU", "KGOU", "KROU", "KOSU", "KWGS", "KWTU", "KWOU", "KAGI",
               "KSJK", "KSMF", "KSRG", "KSOR", "KMUN", "KOAB-FM", "KSBA", "KOAC", "KLCC", "KOPB", "KRVM", "KLFO",
               "KMHD", "KLMF", "KSKF", "KOOZ", "KLCO", "KRBM", "KOPB-FM", "KLFR", "KSRS", "KTBR", "KMPQ", "KTCB",
               "KCPB-FM", "WDIY", "WQLN-FM", "WITF-FM", "WQEJ", "WPSX", "WXPH", "WRTY", "WHYY-FM", "WRTI", "WXPN",
               "WESA", "WQED-FM", "WYEP-FM", "WVIA-FM", "WPSU", "WJAZ", "WVYA", "WRTU", "WRNI", "WRNI-FM", "WLJK",
               "WJWJ-FM", "WSCI", "WLTR", "WHMC-FM", "WEPR", "WNSC-FM", "WRJA-FM", "KESD", "KPSD-FM", "KQSD-FM",
               "KZSD-FM", "KDSD-FM", "KBHE-FM", "KTSD-FM", "KCSD", "KRSD", "KUSD", "WUTC", "WSMC-FM", "WHRS", "WKNQ",
               "WKNP", "WETS-FM", "WUOT", "WPLN", "WKNO-FM", "WMOT", "WPLN-FM", "WFCL", "WTML", "KACU", "KJJP", "KUT",
               "KVLU", "KTXP", "KAMU-FM", "KEOS", "KETR", "KEDT-FM", "KERA", "KKXT", "KNTU", "KERA", "KTEP", "KJJF",
               "KUHF", "KTSU", "KTXI", "KVHL", "KLDN", "KRTS", "KHID", "KXWT", "KPVU", "KNCH-FM", "KSTX", "KPAC",
               "KERA", "KTPR", "KTOT", "KTRL", "KTXK", "KERA", "KVRT", "KWBU-FM", "KMCU", "KERA", "KUSU-FM", "KUSR",
               "KUST", "KPCW", "KCEU", "KUSL", "KUER-FM", "KSGU", "KUSK", "WVIE", "WBTN-FM", "WVPS", "WXLQ", "WNCH",
               "WRVT", "WVPA", "WVPR", "WVTU", "WVTW", "WMRY", "WMVE", "WWVT", "WMLU", "WFFC", "WMRA", "WCNV", "WCHG",
               "WMRL", "WVTR", "WVLS", "WHRO-FM", "WHRV", "WNSB", "WCVE-FM", "WVTF", "WISE-FM", "KZAZ", "KSWS", "KHNW",
               "KNWV", "KNWR", "KNWU", "KLWS", "KMWS", "KQWS", "KNWP", "KWSU", "KFAE-FM", "KEXP-FM", "KUOW-FM",
               "KPBX-FM", "KSFC", "KPLU-FM", "KUOW", "KVTI", "KWWS", "KNWY", "KYVT", "WVBY", "WVPW", "WVPB", "WVMR",
               "WVWV", "WVEP", "WVPM", "WVPG", "WVDS", "WVNP", "WLBL", "WHSA", "WHAD", "WUEC", "WHID", "WPNE", "WOJB",
               "WHHI", "WGTD", "WHLA", "WLSU", "WERN", "WHA", "WHWC", "WVSS", "WUWM", "WYMS", "WRST-FM", "WHBM", "WXPR",
               "WRFW", "WSHS", "WHDI", "WHND", "KUWS", "WHRM", "WLBL-FM", "WXPW", "KUWA", "KBUW", "KUWC", "KDUW",
               "KWRR", "KUWG", "KUWJ", "KUWR", "KUWN", "KUWX", "KUWP", "KUWZ", "KSUW", "KUWD", "KUWT"]


def get_station(station_id):
    url = "http://api.npr.org/v2/stations/search/{}?apiKey={}".format(station_id, API_KEY)
    resp = requests.get(url).json()
    return resp[0] if len(resp) > 0 and isinstance(resp[0], dict) else None


def get_stations():
    for station_id in station_ids:
        station = get_station(station_id)
        if not station:
            continue

        station_url = station["homepage"]
        name = station["name"]
        desc = station["tagline"]
        icon_url = station["logo"]
        stream_urls = tuple([url["href"] for url in station["urls"] if url["type_id"] == "10"])
        if not stream_urls:
            continue

        yield dict(network="NPR",
                   stationUrl=station_url,
                   name=name,
                   streamUrls=stream_urls,
                   description=desc,
                   iconUrl=icon_url,
                   commercials=False)

uniqify = lambda ds: map(dict, set(tuple(sorted(d.items())) for d in ds))


def main():
    stations = list(uniqify(get_stations()))
    print json.dumps(stations, sort_keys=True, indent=4)


if __name__ == "__main__":
    main()
