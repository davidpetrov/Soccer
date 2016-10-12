package scraper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Names {

	public static final HashMap<String, HashMap<String, String>> map = new HashMap<>();

	static {

		HashMap<String, String> bra = new HashMap<>();
		bra.put("ABC", "ABC");
		bra.put("America MG", "Amrica Mineiro");
		bra.put("Atletico GO", "Atltico GO");
		bra.put("Atletico-MG", "Atltico Mineiro");
		bra.put("Atletico-PR", "Atltico PR");
		bra.put("America RN", "Amrica RN");
		bra.put("ASA", "ASA");
		bra.put("Avai", "Ava");
		bra.put("Bahia", "Bahia");
		bra.put("Boa", "Boa");
		bra.put("Botafogo RJ", "Botafogo");
		bra.put("Bragantino", "Bragantino");
		bra.put("Brasil de Pelotas", "Brasil de Pelotas");
		bra.put("Ceara", "Cear");
		bra.put("Chapecoense-SC", "Chapecoense");
		bra.put("Corinthians", "Corinthians");
		bra.put("Coritiba", "Coritiba");
		bra.put("CRB", "CRB");
		bra.put("Criciuma", "Cricima");
		bra.put("Cruzeiro", "Cruzeiro");
		bra.put("Figueirense", "Figueirense");
		bra.put("Flamengo RJ", "Flamengo");
		bra.put("Fluminense", "Fluminense");
		bra.put("Goias", "Gois");
		bra.put("Gremio", "Grmio");
		bra.put("Guaratingueta", "Guaratinguet");
		bra.put("Icasa", "Icasa");
		bra.put("Internacional", "Internacional");
		bra.put("Joinville", "Joinville");
		bra.put("Londrina", "Londrina");
		bra.put("Luverdense", "Luverdense");
		bra.put("Macae", "Maca");
		bra.put("Mogi Mirim", "Mogi Mirim");
		bra.put("Nautico", "Nutico");
		bra.put("Oeste", "Oeste");
		bra.put("Palmeiras", "Palmeiras");
		bra.put("Parana", "Paran");
		bra.put("Paysandu PA", "Paysandu");
		bra.put("Ponte Preta", "Ponte Preta");
		bra.put("Portuguesa", "Portuguesa");
		bra.put("Santos", "Santos");
		bra.put("Sampaio Correa", "Sampaio Corra");
		bra.put("Santa Cruz", "Santa Cruz");
		bra.put("Sao Caetano", "So Caetano");
		bra.put("Sao Paulo", "So Paulo");
		bra.put("Sport Recife", "Sport Recife");
		bra.put("Tupi", "Tupi");
		bra.put("Vasco", "Vasco da Gama");
		bra.put("Vila Nova FC", "Vila Nova");
		bra.put("Vitoria", "Vitria");

		map.put("BRA", bra);
		map.put("BRB", bra);
		

		HashMap<String, String> swe = new HashMap<>();
		swe.put("AIK", "AIK");
		swe.put("Atvidabergs", "tvidaberg");
		swe.put("Brommapojkarna", "Brommapojkarna");
		swe.put("Djurgarden", "Djurgrden");
		swe.put("Elfsborg", "Elfsborg");
		swe.put("Falkenbergs", "Falkenberg");
		swe.put("Gefle", "Gefle");
		swe.put("Goteborg", "IFK Gteborg");
		swe.put("Hacken", "Hcken");
		swe.put("Halmstad", "Halmstad");
		swe.put("Hammarby", "Hammarby");
		swe.put("Helsingborg", "Helsingborg");
		swe.put("Jonkopings", "Jnkpings Sdra");
		swe.put("Kalmar", "Kalmar");
		swe.put("Malmo FF", "Malm FF");
		swe.put("Mjallby", "Mjllby");
		swe.put("Norrkoping", "Norrkping");
		swe.put("Orebro", "rebro");
		swe.put("Osters", "ster");
		swe.put("Ostersunds", "stersunds FK");
		swe.put("Sundsvall", "GIF Sundsvall");
		swe.put("Syrianska", "Syrianska FC");

		map.put("SWE", swe);

		HashMap<String, String> nor = new HashMap<>();
		nor.put("Aalesund", "Aalesund");
		nor.put("Bodo/Glimt", "Bod / Glimt");
		nor.put("Brann", "Brann");
		nor.put("Haugesund", "Haugesund");
		nor.put("Lillestrom", "Lillestrm");
		nor.put("Mjondalen", "Mjndalen");
		nor.put("Molde", "Molde");
		nor.put("Odd", "Odd");
		nor.put("Rosenborg", "Rosenborg");
		nor.put("Sandefjord", "Sandefjord");
		nor.put("Sandnes", "Sandnes Ulf");
		nor.put("Sarpsborg 08", "Sarpsborg 08");
		nor.put("Sogndal", "Sogndal");
		nor.put("Stabaek", "Stabk");
		nor.put("Start", "Start");
		nor.put("Stromsgodset", "Strmsgodset");
		nor.put("Tromso", "Troms");
		nor.put("Valerenga", "Vlerenga");
		nor.put("Viking", "Viking");

		map.put("NOR", nor);

		HashMap<String, String> arg = new HashMap<>();
		arg.put("Aldosivi", "Aldosivi");
		arg.put("All Boys", "All Boys");
		arg.put("Almagro", "Almagro");
		arg.put("Argentinos Jrs", "Argentinos Juniors");
		arg.put("Atletico Parana", "Atltico Paran");
		arg.put("Atl. Tucuman", "Atltico Tucumn");
		arg.put("Boca Unidos", "Boca Unidos");
		arg.put("Brown Adrogue", "Brown de Adrogu");
		arg.put("Central Cordoba", "Central Crdoba SdE");
		arg.put("Chacarita Juniors", "Chacarita Juniors");
		arg.put("Colon Santa FE", "Coln");
		arg.put("Crucero del Norte", "Crucero del Norte");
		arg.put("Douglas Haig", "Douglas Haig");
		arg.put("Ferro", "Ferro Carril Oeste");
		arg.put("Gimnasia Jujuy", "Gimnasia Jujuy");
		arg.put("Gimnasia Mendoza", "Gimnasia Mendoza");
		arg.put("Guarani Antonio Franco", "Guaran A. Franco");
		arg.put("Guillermo Brown", "Guillermo Brown");
		arg.put("Huracan", "Huracn");
		arg.put("Ind. Rivadavia", "Independiente Rivadavia");
		arg.put("Instituto", "Instituto");
		arg.put("Juventud Unida Gualeguaychu", "Juventud Unida G.");
		arg.put("Juventud Unida Universitario", "Juventud Unida Univ.");
		arg.put("Los Andes", "Los Andes");
		arg.put("Nueva Chicago", "Nueva Chicago");
		arg.put("Patronato", "Patronato");
		arg.put("San Martin S.J.", "San Martn San Juan");
		arg.put("Santamarina", "Deportivo Santamarina");
		arg.put("Sarmiento Junin", "Sarmiento");
		arg.put("Sportivo Belgrano", "Sportivo Belgrano");
		arg.put("Sportivo Estudiantes", "Estudiantes de San Luis");
		arg.put("Talleres Cordoba", "Talleres Crdoba");
		arg.put("Temperley", "Temperley");
		arg.put("Union de Santa Fe", "Unin Santa Fe");
		arg.put("Union Mar del Plata", "Unin Mar del Plata");
		arg.put("Villa Dalmine", "Villa Dlmine");

		map.put("ARG", arg);

		HashMap<String, String> ice = new HashMap<>();
		ice.put("Akranes", "A");
		ice.put("Breidablik", "Breidablik");
		ice.put("Fjolnir", "Fjlnir");
		ice.put("Fram", "Fram");
		ice.put("Fylkir", "Fylkir");
		ice.put("Hafnarfjordur", "FH");
		ice.put("Keflavik", "Keflavk");
		ice.put("KR Reykjavik", "KR");
		ice.put("Leiknir", "Leiknir Reykjavk");
		ice.put("Olafsvik", "Vkingur lafsvk");
		ice.put("Stjarnan", "Stjarnan");
		ice.put("Thor Akureyri", "Thr");
		ice.put("Throttur", "Thrttur Reykjavk");
		ice.put("Valur", "Valur");
		ice.put("Vestmannaeyjar", "BV");
		ice.put("Vikingur Reykjavik", "Vkingur Reykjavk");

		map.put("ICE", ice);

		HashMap<String, String> fin = new HashMap<>();
		fin.put("HIFK", "HIFK");
		fin.put("HJK", "HJK");
		fin.put("Honka", "Honka");
		fin.put("Ilves", "Ilves");
		fin.put("Inter Turku", "Inter Turku");
		fin.put("Jaro", "Jaro");
		fin.put("KTP", "KTP");
		fin.put("KuPS", "KuPS");
		fin.put("Lahti", "Lahti");
		fin.put("Mariehamn", "Mariehamn");
		fin.put("MyPa", "MYPA");
		fin.put("PK-35 Vantaa", "PK-35 Vantaa");
		fin.put("PS Kemi", "PS Kemi");
		fin.put("Rovaniemi", "RoPS");
		fin.put("SJK", "SJK");
		fin.put("TPS", "TPS");
		fin.put("VPS", "VPS");

		map.put("FIN", fin);

		HashMap<String, String> jp = new HashMap<>();
		jp.put("Albirex Niigata", "Albirex Niigata");
		jp.put("Avispa Fukuoka", "Avispa Fukuoka");
		jp.put("C-Osaka", "Cerezo Osaka");
		jp.put("FC Tokyo", "Tokyo");
		jp.put("G-Osaka", "Gamba Osaka");
		jp.put("Hiroshima", "Sanfrecce Hiroshima");
		jp.put("Iwata", "Jbilo Iwata");
		jp.put("Kashima", "Kashima Antlers");
		jp.put("Kashiwa", "Kashiwa Reysol");
		jp.put("Kawasaki Frontale", "Kawasaki Frontale");
		jp.put("Kobe", "Vissel Kobe");
		jp.put("Kofu", "Ventforet Kofu");
		jp.put("Montedio Yamagata", "Montedio Yamagata");
		jp.put("Nagoya", "Nagoya Grampus");
		jp.put("Omiya Ardija", "Omiya Ardija");
		jp.put("Shimizu", "Shimizu S-Pulse");
		jp.put("Shonan", "Shonan Bellmare");
		jp.put("Tokushima", "Tokushima Vortis");
		jp.put("Tosu", "Sagan Tosu");
		jp.put("Urawa", "Urawa Reds");
		jp.put("Vegalta Sendai", "Vegalta Sendai");
		jp.put("Yamaga", "Matsumoto Yamaga");
		jp.put("Yokohama M.", "Yokohama F. Marinos");

		map.put("JP", jp);

		HashMap<String, String> sp = new HashMap<>();
		sp.put("Alaves", "Deportivo Alavs");
		sp.put("Albacete", "Albacete");
		sp.put("Alcorcon", "Alcorcn");
		sp.put("Almeria", "Almera");
		sp.put("Ath Bilbao B", "Athletic Club II");
		sp.put("Barcelona B", "Barcelona II");
		sp.put("Betis", "Real Betis");
		sp.put("Cordoba", "Crdoba");
		sp.put("Dep. La Coruna", "Deportivo La Corua");
		sp.put("Eibar", "Eibar");
		sp.put("Elche", "Elche");
		sp.put("Gijon", "Sporting Gijn");
		sp.put("Gimnastic", "Gimnstic Tarragona");
		sp.put("Girona", "Girona");
		sp.put("Hercules", "Hrcules");
		sp.put("Huesca", "Huesca");
		sp.put("Jaen", "Real Jan");
		sp.put("Las Palmas", "Las Palmas");
		sp.put("Leganes", "Legans");
		sp.put("Llagostera", "Llagostera");
		sp.put("Lugo", "Lugo");
		sp.put("Mallorca", "Mallorca");
		sp.put("Mirandes", "Mirands");
		sp.put("Murcia", "Real Murcia");
		sp.put("Numancia", "Numancia");
		sp.put("Osasuna", "Osasuna");
		sp.put("Ponferradina", "Ponferradina");
		sp.put("Real Madrid B", "Real Madrid II");
		sp.put("Recreativo Huelva", "Recreativo Huelva");
		sp.put("R. Oviedo", "Real Oviedo");
		sp.put("Sabadell", "Sabadell");
		sp.put("Santander", "Racing Santander");
		sp.put("Tenerife", "Tenerife");
		sp.put("Valladolid", "Real Valladolid");
		sp.put("Zaragoza", "Real Zaragoza");

		map.put("SP2", sp);

		HashMap<String, String> swi = new HashMap<>();
		swi.put("Aarau", "Aarau");
		swi.put("Basel", "Basel");
		swi.put("Grasshoppers", "Grasshopper");
		swi.put("Lausanne", "Lausanne Sport");
		swi.put("Lugano", "Lugano");
		swi.put("Luzern", "Luzern");
		swi.put("Sion", "Sion");
		swi.put("St. Gallen", "St. Gallen");
		swi.put("Thun", "Thun");
		swi.put("Vaduz", "Vaduz");
		swi.put("Young Boys", "Young Boys");
		swi.put("Zurich", "Zrich");

		map.put("SWI", swi);

		HashMap<String, String> den = new HashMap<>();
		den.put("Aalborg", "AaB");
		den.put("Aarhus", "AGF");
		den.put("Brondby", "Brndby");
		den.put("Esbjerg", "Esbjerg");
		den.put("FC Copenhagen", "Kbenhavn");
		den.put("Hobro", "Hobro");
		den.put("Midtjylland", "Midtjylland");
		den.put("Nordsjaelland", "Nordsjlland");
		den.put("Odense", "OB");
		den.put("Randers FC", "Randers");
		den.put("Silkeborg", "Silkeborg");
		den.put("Sonderjyske", "SnderjyskE");
		den.put("Vestsjaelland", "Vestsjlland");
		den.put("Viborg", "Viborg");
		map.put("DEN", den);

		HashMap<String, String> aus = new HashMap<>();
		aus.put("AC Wolfsberger", "Wolfsberger AC");
		aus.put("Admira", "Admira");
		aus.put("Altach", "Rheindorf Altach");
		aus.put("Austria Vienna", "Austria Wien");
		aus.put("Grodig", "Grdig");
		aus.put("Mattersburg", "Mattersburg");
		aus.put("Neustadt", "Wiener Neustadt");
		aus.put("Rapid Vienna", "Rapid Wien");
		aus.put("Ried", "Ried");
		aus.put("Salzburg", "Salzburg");
		aus.put("Sturm Graz", "Sturm Graz");
		aus.put("Wacker Innsbruck", "Wacker Innsbruck");
		map.put("AUS", aus);

		HashMap<String, String> cze = new HashMap<>();
		cze.put("Bohemians 1905", "Bohemians 1905");
		cze.put("Brno", "Zbrojovka Brno");
		cze.put("Ceske Budejovice", "esk Budjovice");
		cze.put("Dukla Prague", "Dukla Praha");
		cze.put("Hradec Kralove", "Hradec Krlov");
		cze.put("Jablonec", "Jablonec");
		cze.put("Jihlava", "Vysoina Jihlava");
		cze.put("Liberec", "Slovan Liberec");
		cze.put("Mlada Boleslav", "Mlad Boleslav");
		cze.put("Ostrava", "Bank Ostrava");
		cze.put("Plzen", "Viktoria Plze");
		cze.put("Pribram", "Pbram");
		cze.put("Sigma Olomouc", "Sigma Olomouc");
		cze.put("Slavia Prague", "Slavia Praha");
		cze.put("Slovacko", "Slovcko");
		cze.put("Sparta Prague", "Sparta Praha");
		cze.put("Teplice", "Teplice");
		cze.put("Zlin", "Zln");
		cze.put("Znojmo", "Znojmo");
		map.put("CZE", cze);

		HashMap<String, String> rus = new HashMap<>();
		rus.put("Amkar", "Amkar Perm'");
		rus.put("CSKA Moscow", "CSKA Moskva");
		rus.put("Dynamo Moscow", "Dinamo Moskva");
		rus.put("FK Anzi Makhackala", "Anzhi");
		rus.put("FK Krylya Sovetov Samara", "Krylya Sovetov");
		rus.put("FK Rostov", "Rostov");
		rus.put("Krasnodar", "Krasnodar");
		rus.put("Kuban", "Kuban' Krasnodar");
		rus.put("Lokomotiv Moscow", "Lokomotiv Moskva");
		rus.put("M. Saransk", "Mordovia Saransk");
		rus.put("Rubin Kazan", "Rubin Kazan'");
		rus.put("Spartak Moscow", "Spartak Moskva");
		rus.put("Terek Grozni", "Terek Grozny");
		rus.put("Tomsk", "Tom' Tomsk");
		rus.put("Ufa", "Ufa");
		rus.put("Ural", "Ural");
		rus.put("Zenit Petersburg", "Zenit");
		rus.put("Arsenal Tula", "Arsenal Tula");
		rus.put("T. Moscow", "Torpedo Moskva");
		rus.put("FK Krylya Sovetov Samara", "Krylya Sovetov");
		rus.put("Volga N. Novgorod", "Volga");
		map.put("RUS", rus);

		HashMap<String, String> ned = new HashMap<>();
		ned.put("Ajax", "Ajax");
		ned.put("AZ Alkmaar", "AZ");
		ned.put("Cambuur", "Cambuur");
		ned.put("Den Haag", "ADO Den Haag");
		ned.put("Excelsior", "Excelsior");
		ned.put("Feyenoord", "Feyenoord");
		ned.put("Graafschap", "De Graafschap");
		ned.put("Groningen", "Groningen");
		ned.put("Heerenveen", "Heerenveen");
		ned.put("Heracles", "Heracles");
		ned.put("Nijmegen", "NEC");
		ned.put("PSV", "PSV");
		ned.put("Roda", "Roda JC");
		ned.put("Twente", "Twente");
		ned.put("Utrecht", "Utrecht");
		ned.put("Vitesse", "Vitesse");
		ned.put("Willem II", "Willem II");
		ned.put("Zwolle", "PEC Zwolle");
		ned.put("Breda", "NAC Breda");
		ned.put("Dordrecht", "Dordrecht");
		ned.put("G.A. Eagles", "Go Ahead Eagles");
		ned.put("Waalwijk", "RKC Waalwijk");
		ned.put("Venlo", "VVV");
		map.put("NED", ned);

		HashMap<String, String> por = new HashMap<>();
		por.put("Academica", "Acadmica");
		por.put("Arouca", "Arouca");
		por.put("Belenenses", "Belenenses");
		por.put("Benfica", "Benfica");
		por.put("Boavista", "Boavista");
		por.put("Braga", "Sporting Braga");
		por.put("Estoril", "Estoril");
		por.put("FC Porto", "Porto");
		por.put("Ferreira", "Paos de Ferreira");
		por.put("Guimaraes", "Vitria Guimares");
		por.put("Maritimo", "Martimo");
		por.put("Moreirense", "Moreirense");
		por.put("Nacional", "Nacional");
		por.put("Rio Ave", "Rio Ave");
		por.put("Setubal", "Vitria Setbal");
		por.put("Sporting", "Sporting CP");
		por.put("Tondela", "Tondela");
		por.put("U. Madeira", "Unio Madeira");
		por.put("Gil Vicente", "Gil Vicente");
		por.put("Penafiel", "Penafiel");
		por.put("Olhanense", "Olhanense");
		por.put("Beira Mar", "Beira-Mar");
		por.put("Feirense", "Feirense");
		por.put("Leiria", "Unio de Leiria");
		map.put("POR", por);

		HashMap<String, String> bel = new HashMap<>();
		bel.put("Anderlecht", "Anderlecht");
		bel.put("Charleroi", "Sporting Charleroi");
		bel.put("Club Brugge KV", "Club Brugge");
		bel.put("Eupen", "AS Eupen");
		bel.put("Genk", "Genk");
		bel.put("Gent", "Gent");
		bel.put("Kortrijk", "Kortrijk");
		bel.put("KV Mechelen", "Mechelen");
		bel.put("Leuven", "OH Leuven");
		bel.put("Lokeren", "Lokeren");
		bel.put("Mouscron", "Royal Excel Mouscron");
		bel.put("Oostende", "KV Oostende");
		bel.put("St. Liege", "Standard Lige");
		bel.put("St. Truiden", "Sint-Truiden");
		bel.put("Waasland-Beveren", "Waasland-Beveren");
		bel.put("Waregem", "Zulte-Waregem");
		bel.put("Westerlo", "Westerlo");
		bel.put("Cercle Brugge KSV", "Cercle Brugge");
		bel.put("Lierse", "Lierse");
		bel.put("Mons", "Mons");
		map.put("BEL", bel);

		HashMap<String, String> fr2 = new HashMap<>();
		fr2.put("AC Ajaccio", "Ajaccio");
		fr2.put("Auxerre", "Auxerre");
		fr2.put("Amiens", "Amiens SC");
		fr2.put("Bourg Peronnas", "Bourg en Bresse");
		fr2.put("Brest", "Brest");
		fr2.put("Clermont", "Clermont");
		fr2.put("Creteil", "Crteil");
		fr2.put("Dijon", "Dijon");
		fr2.put("Evian TG", "Evian TG");
		fr2.put("Laval", "Laval");
		fr2.put("Le Havre", "Le Havre");
		fr2.put("Lens", "Lens");
		fr2.put("Metz", "Metz");
		fr2.put("Nancy", "Nancy");
		fr2.put("Nimes", "Nmes");
		fr2.put("Niort", "Niort");
		fr2.put("Paris FC", "Paris");
		fr2.put("Red Star", "Red Star");
		fr2.put("Reims", "Reims");
		fr2.put("Sochaux", "Sochaux");
		fr2.put("Strasbourg", "Strasbourg");
		fr2.put("Tours", "Tours");
		fr2.put("Valenciennes", "Valenciennes");
		fr2.put("Angers", "Angers");
		fr2.put("Arles-Avignon", "Arles");
		fr2.put("Chateauroux", "Chteauroux");
		fr2.put("GFC Ajaccio", "Gazlec Ajaccio");
		fr2.put("Orleans", "Orlans");
		fr2.put("Troyes", "Troyes");
		fr2.put("CA Bastia", "CA Bastia");
		fr2.put("Caen", "Caen");
		fr2.put("Istres", "Istres");

		map.put("FR2", fr2);

		HashMap<String, String> usa = new HashMap<>();
		usa.put("Chicago Fire", "Chicago Fire");
		usa.put("Colorado Rapids", "Colorado Rapids");
		usa.put("Columbus Crew", "Columbus Crew");
		usa.put("DC United", "DC United");
		usa.put("FC Dallas", "Dallas");
		usa.put("Houston Dynamo", "Houston Dynamo");
		usa.put("Los Angeles Galaxy", "LA Galaxy");
		usa.put("Montreal Impact", "Montreal Impact");
		usa.put("New England Revolution", "New England");
		usa.put("New York City", "New York City");
		usa.put("New York Red Bulls", "New York RB");
		usa.put("Orlando City", "Orlando City");
		usa.put("Philadelphia Union", "Philadelphia Union");
		usa.put("Portland Timbers", "Portland Timbers");
		usa.put("Real Salt Lake", "Real Salt Lake");
		usa.put("San Jose Earthquakes", "SJ Earthquakes");
		usa.put("Seattle Sounders", "Seattle Sounders");
		usa.put("Sporting Kansas City", "Sporting KC");
		usa.put("Toronto FC", "Toronto");
		usa.put("Vancouver Whitecaps", "Vancouver Whitecaps");
		usa.put("Chivas USA", "Chivas USA");
		map.put("USA", usa);

		HashMap<String, String> tur = new HashMap<>();
		tur.put("Akhisar Genclik Spor", "Akhisar Belediyespor");
		tur.put("Antalyaspor", "Antalyaspor");
		tur.put("Basaksehir", "stanbul Baakehir");
		tur.put("Besiktas", "Beikta");
		tur.put("Bursaspor", "Bursaspor");
		tur.put("Eskisehirspor", "Eskiehirspor");
		tur.put("Fenerbahce", "Fenerbahe");
		tur.put("Galatasaray", "Galatasaray");
		tur.put("Gaziantepspor", "Gaziantepspor");
		tur.put("Genclerbirligi", "Genlerbirlii");
		tur.put("Kasimpasa", "Kasmpaa");
		tur.put("Kayserispor", "Kayserispor");
		tur.put("Konyaspor", "Konyaspor");
		tur.put("Mersin", "Mersin dmanyurdu");
		tur.put("Osmanlispor", "Osmanlspor");
		tur.put("Rizespor", "Rizespor");
		tur.put("Sivasspor", "Sivasspor");
		tur.put("Trabzonspor", "Trabzonspor");
		tur.put("Balikesirspor", "Balkesirspor");
		tur.put("Kardemir Karabuk", "Karabkspor");
		tur.put("Kayseri Erciyesspor", "Kayseri Erciyesspor");
		tur.put("Elazigspor", "Elazspor");
		map.put("TUR", tur);
		
		
		HashMap<String, String> gre = new HashMap<>();
		gre.put("AEK Athens FC", "AEK Athens");
		gre.put("Asteras Tripolis", "Asteras Tripolis");
		gre.put("Atromitos", "Atromitos");
		gre.put("Giannina", "PAS Giannina");
		gre.put("Iraklis", "Iraklis");
		gre.put("Kalloni", "Kalloni");
		gre.put("Levadiakos", "Levadiakos");
		gre.put("Olympiakos Piraeus", "Olympiakos Piraeus");
		gre.put("Panathinaikos", "Panathinaikos");
		gre.put("Panetolikos", "Panaitolikos");
		gre.put("Panionios", "Panionios");
		gre.put("Panthrakikos", "Panthrakikos");
		gre.put("PAOK", "PAOK");
		gre.put("Platanias FC", "Platanias");
		gre.put("Skoda Xanthi", "Xanthi");
		gre.put("Veria", "Veria");
		gre.put("Ergotelis", "Ergotelis");
		gre.put("Kerkyra", "Kerkyra");
		gre.put("Niki Volos", "Niki Volos");
		gre.put("OFI Crete", "OFI");
		gre.put("Aris", "Aris");
		gre.put("Smyrnis", "Apollon Smirnis");
		map.put("GRE", gre);
		
		
		HashMap<String, String> hun = new HashMap<>();
		hun.put("Bekescsaba 1912", "Bkscsaba");
		hun.put("Debrecen", "Debrecen");
		hun.put("DVTK", "Disgyr");
		hun.put("Ferencvaros", "Ferencvros");
		hun.put("Haladas", "Szombathelyi Halads");
		hun.put("Honved", "Honvd");
		hun.put("MTK Budapest", "MTK");
		hun.put("Paks", "Paksi SE");
		hun.put("Puskas Academy", "Pusks FC");
		hun.put("Ujpest", "jpest");
		hun.put("Vasas", "Vasas");
		hun.put("Videoton", "Videoton");
		hun.put("Dunaujvaros PASE", "Dunajvros-Plhalma");
		hun.put("Gyor", "Gyri ETO");
		hun.put("Kecskemeti TE", "Kecskemti TE");
		hun.put("Lombard Papa", "Ppa");
		hun.put("Nyiregyhaza", "Nyregyhza Spartacus");
		hun.put("Pecsi MFC", "Pcsi MFC");
		hun.put("Kaposvar", "Kaposvri Rkczi");
		hun.put("Mezokovesd-Zsory", "Mezkvesd-Zsry");
		map.put("HUN", hun);
		
		HashMap<String, String> d2 = new HashMap<>();
		d2.put("Arminia Bielefeld", "Arminia Bielefeld");
		d2.put("Bochum", "Bochum");
		d2.put("Braunschweig", "Eintracht Braunschweig");
		d2.put("Duisburg", "MSV Duisburg");
		d2.put("Dusseldorf", "Fortuna Dsseldorf");
		d2.put("Freiburg", "Freiburg");
		d2.put("FSV Frankfurt", "FSV Frankfurt");
		d2.put("Greuther Furth", "Greuther Frth");
		d2.put("Heidenheim", "Heidenheim");
		d2.put("Kaiserslautern", "Kaiserslautern");
		d2.put("Karlsruher", "Karlsruher SC");
		d2.put("Munich 1860", "1860 Mnchen");
		d2.put("Nurnberg", "Nrnberg");
		d2.put("Paderborn", "Paderborn");
		d2.put("RB Leipzig", "RB Leipzig");
		d2.put("Sandhausen", "Sandhausen");
		d2.put("St. Pauli", "St. Pauli");
		d2.put("Union Berlin", "Union Berlin");
		d2.put("Aalen", "Aalen");
		d2.put("Aue", "Erzgebirge Aue");
		d2.put("Darmstadt", "Darmstadt 98");
		d2.put("Ingolstadt", "Ingolstadt");
		d2.put("1. FC Koln", "Kln");
		d2.put("Energie Cottbus", "Energie Cottbus");
		d2.put("SG Dynamo Dresden", "Dynamo Dresden");
		map.put("D2", d2);
		
		HashMap<String, String> it2 = new HashMap<>();
		it2.put("Ascoli", "Ascoli");
		it2.put("Avellino", "Avellino");
		it2.put("Bari", "Bari 1908");
		it2.put("Brescia", "Brescia");
		it2.put("Cagliari", "Cagliari");
		it2.put("Cesena", "Cesena");
		it2.put("Como", "Como");
		it2.put("Crotone", "Crotone");
		it2.put("Entella", "Virtus Entella");
		it2.put("Lanciano", "Virtus Lanciano");
		it2.put("Latina", "Latina");
		it2.put("Livorno", "Livorno");
		it2.put("Modena", "Modena");
		it2.put("Novara", "Novara");
		it2.put("Perugia", "Perugia");
		it2.put("Pescara", "Pescara");
		it2.put("Pro Vercelli", "Pro Vercelli");
		it2.put("Salernitana", "Salernitana");
		it2.put("Spezia", "Spezia");
		it2.put("Ternana", "Ternana");
		it2.put("Trapani", "Trapani");
		it2.put("Vicenza", "Vicenza");
		it2.put("Bologna", "Bologna");
		it2.put("Carpi", "Carpi");
		it2.put("Catania", "Catania");
		it2.put("Cittadella", "Cittadella");
		it2.put("Frosinone", "Frosinone");
		it2.put("Varese", "Varese");
		it2.put("Empoli", "Empoli");
		it2.put("Juve Stabia", "Juve Stabia");
		it2.put("Padova", "Padova");
		it2.put("Palermo", "Palermo");
		it2.put("Reggio Calabria", "Reggina");
		it2.put("Siena", "Robur Siena");
		map.put("IT2", it2);
		
		HashMap<String, String> pol = new HashMap<>();
		pol.put("Cracovia", "Cracovia Krakw");
		pol.put("Gornik Z.", "Grnik Zabrze");
		pol.put("Jagiellonia", "Jagiellonia Biaystok");
		pol.put("Korona Kielce", "Korona Kielce");
		pol.put("Lech Poznan", "Lech Pozna");
		pol.put("Lechia Gdansk", "Lechia Gdask");
		pol.put("Leczna", "Grnik czna");
		pol.put("Legia", "Legia Warszawa");
		pol.put("Piast Gliwice", "Piast Gliwice");
		pol.put("Podbeskidzie", "Podbeskidzie");
		pol.put("Pogon Szczecin", "Pogo Szczecin");
		pol.put("Ruch", "Ruch Chorzw");
		pol.put("Slask Wroclaw", "lsk Wrocaw");
		pol.put("Termalica B-B.", "Nieciecza");
		pol.put("Wisla", "Wisa Krakw");
		pol.put("Zaglebie", "Zagbie Lubin");
		pol.put("GKS Belchatow", "Bechatw");
		pol.put("Zawisza", "Zawisza Bydgoszcz");
		pol.put("Widzew Lodz", "Widzew d");
		pol.put("Ruch", "Ruch Chorzw");
		map.put("POL", pol);
		
		
		HashMap<String, String> eng = new HashMap<>();
		eng.put("Arsenal", "Arsenal");
		eng.put("Bournemouth", "AFC Bournemouth");
		eng.put("Burnley", "Burnley");
		eng.put("Chelsea", "Chelsea");
		eng.put("Crystal Palace", "Crystal Palace");
		eng.put("Everton", "Everton");
		eng.put("Hull City", "Hull City");
		eng.put("Leicester", "Leicester City");
		eng.put("Liverpool", "Liverpool");
		eng.put("Manchester City", "Manchester City");
		eng.put("Manchester United", "Manchester United");
		eng.put("Middlesbrough", "Middlesbrough");
		eng.put("Southampton", "Southampton");
		eng.put("Stoke City", "Stoke City");
		eng.put("Sunderland", "Sunderland");
		eng.put("Swansea", "Swansea City");
		eng.put("Tottenham", "Tottenham Hotspur");
		eng.put("Watford", "Watford");
		eng.put("West Brom", "West Bromwich Albion");
		eng.put("West Ham", "West Ham United");
		eng.put("Aston Villa", "Aston Villa");
		eng.put("Newcastle Utd", "Newcastle United");
		eng.put("Norwich", "Norwich City");
//		eng.put("", "");
//		eng.put("", "");
//		eng.put("", "");
//		eng.put("", "");
//		eng.put("", "");
//		eng.put("", "");
//		eng.put("", "");
//		eng.put("", "");
//		eng.put("", "");
//		eng.put("", "");
		map.put("ENG", eng);
	}

	public static String getSoccerName(String competition, String name) {
		return map.get(competition).get(name);
	}

	public static String getOddsName(String competition, String name) {

		return map.get(competition).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)).get(name);
	}

	public static void test(String competition) {
		for (Entry<String, String> i : map.get(competition).entrySet()) {
			if (!getOddsName(competition, getSoccerName(competition, i.getKey())).equals(i.getKey())) {
				System.out.println(i.getKey());
			}
		}
	}

}
