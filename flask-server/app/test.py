import json
import requests
import os

API_KEY = os.environ.get("EDIBUS_API_KEY")

url = 'https://api.steampowered.com/IDOTA2Match_570/'

essential_parameters = dict(
	key = API_KEY,
	language = "en_us",
	format = "JSON"
	)

 # url = 'https://api.steampowered.com/IDOTA2Match_570/GetMatchDetails/V001/?match_id=521490285&key=' + 'API_KEY'


# GetMatchHistory

# string argument has to be 1 or 0

# specific_hero => Search for matches with a specific hero being played (hero ID, not name, see HEROES below)
# mode_of_game => Search for matches of a given mode (see below)
# skill_level => 0 for any, 1 for normal, 2 for high, 3 for very high skill (default is 0)
# number_of_players => the minimum number of players required in the match
# specific_account => Search for all matches for the given user (32-bit or 64-bit steam ID)
# specific_league => matches for a particular league
# start_from_id => Start the search at the indicated match id, descending
# number_of_matches => Maximum is 25 matches (default is 25)
# tournament_games_only => set to only show tournament games

# 4294967295 private account

def getMatchHistory(specific_hero = None, mode_of_game = None, skill_level = 3, number_of_players = 10, specific_account = None, specific_league = None, start_from_id = None, number_of_matches = 25, string = '1'):

	specific_url = 'GetMatchHistory/V001/'

	parameters = dict(
		hero_id = specific_hero,
		game_mode = mode_of_game,
		skill = skill_level,
		min_players = number_of_players,
		account_id = specific_account,
		league_id = specific_league,
		start_at_match_id = start_from_id,
		matches_requested = number_of_matches,
		tournament_games_only = string
	)

	parameters.update(essential_parameters)

	response = requests.get(url = url + specific_url, params = parameters)
	print response
	return response.json()


def getMatchDetails(specific_match):

	specific_url = 'GetMatchDetails/V001/'

	parameters =dict(
		match_id = specific_match
	)

	parameters.update(essential_parameters)

	response = requests.get(url = url + specific_url, params = parameters)
	print response
	return response.json()




# open a file

f = open("match_history.txt", "w")

match_history_json = getMatchHistory()

json.dump(match_history_json, f, sort_keys = True, indent = 4, ensure_ascii = False)

f.close()

f = open("game_details.txt", "w")

game_details_json = getMatchDetails(789645621)

json.dump(game_details_json, f, sort_keys = True, indent = 4, ensure_ascii = False)