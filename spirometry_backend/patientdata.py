import monitoringmonths
from datetime import timedelta, date


def daterange(start_date, end_date):
    """Generate an iterator for every day between two days
    
    Arguments:
        start_date {datetime.date} -- Start date (inclusive)
        end_date {[type]} -- End date (exclusive)
    """

    for n in range(int((end_date - start_date).days)):
        yield start_date + timedelta(days=n)


class PatientData:
    """
    This is the main data store for all the patient's retrieved study data. It is organized by entry using 
    the unique entry IDs in the ecp_hs database. It has five main indexes. The first, the __data store,
    stores the PatientDataEntry objects by entry ID. The __date_to_indexes dictionary maps the dates of the
    data entries to the entry IDs corresponding to that date, in case there's more than one. The next three
    store the entries containing each of the three respective types of data: spirometry, pulse-oximetry, and
    survey responses. Each PatientDataEntry object is only created once and then each data store gets a reference
    to it.
    """


    def __init__(self):
        """PatientData constructor
        """

        self.__date_to_indexes = {}
        self.__data = {}
        self.__spiro_data = SpiroData()
        self.__pulse_data = PulseData()
        self.__survey_data = SurveyData()

    def new_spiro_data_entry(self, date, entry_id, fev11, fev12, fev13, fev14, fev15, fev16, is_variance=False, variance_counter=None):
        """Add a new spirometry entry
        
        Arguments:
            date {datetime.date} -- Date of the entry
            entry_id {str} -- The entry ID from the database
            fev11 {str} -- The first FEV1 data entry
            fev12 {str} -- The second FEV1 data entry
            fev13 {str} -- The third FEV1 data entry
            fev14 {str} -- The fourth FEV1 data entry
            fev15 {str} -- The fifth FEV1 data entry
            fev16 {str} -- The sixth FEV1 data entry
        
        Keyword Arguments:
            is_variance {bool} -- Whether there is a detected variance for the entry (default: {False})
            variance_counter {int} -- Whether this is one of the four followup tests after a variance is detected (a number between 1 and 4 inclusive), should only be set if is_variance=False (default: {None})
        
        Returns:
            patientdata.SpiroDataEntry -- The newly-created entry
        """

        entry = self.get_entry(date, entry_id)
        entry.set_spiro_data_entry(fev11, fev12, fev13, fev14,
                                   fev15, fev16, is_variance, variance_counter)
        self.__spiro_data.add_entry(entry)
        return entry.get_spiro_data_entry()

    def new_pulse_data_entry(self, date, entry_id, minrate, maxrate, lowestsat, timeabnormal, timeminrate, is_pulse_abnormal, is_o2sat_abnormal):
        """Add a new pulse-oximetry entry
        
        Arguments:
            date {datetime.date} -- Date of the entry
            entry_id {str} -- The entry ID from the database
            minrate {str} -- The minimum heartrate detected for the entry
            maxrate {str} -- The maximum heartrate detected for the entry
            lowestsat {str} -- The lowest oxygen saturation detected for the entry
            timeabnormal {str} -- [description]
            timeminrate {str} -- [description]
            is_pulse_abnormal {int} -- Whether the heartrate detected is abnormal (0 if False, 1 if True) 
            is_o2sat_abnormal {int} -- Whether the oxygen saturation detected is abnormal (0 if False, 1 if slightly abnormal, 2 if dangerously abnormal)
        
        Returns:
            patientdata.PulseDataEntry -- The newly-created entry
        """

        entry = self.get_entry(date, entry_id)
        entry.set_pulse_data_entry(
            minrate, maxrate, lowestsat, timeabnormal, timeminrate, is_pulse_abnormal, is_o2sat_abnormal)
        self.__pulse_data.add_entry(entry)
        return entry.get_pulse_data_entry()

    def new_survey_data_entry(self, date, entry_id):
        """Add a new survey entry. Unlike the other new entry methods in this class, this one only creates the entry object. The survey responses need to be added using the object this method returns.
        
        Arguments:
            date {datetime.date} -- Date of the entry
            entry_id {str} -- The entry ID from the database
        
        Returns:
            patientdata.SurveyDataEntry -- The newly-created entry
        """

        entry = self.get_entry(date, entry_id)
        entry.set_survey_data_entry()
        self.__survey_data.add_entry(entry)
        return entry.get_survey_data_entry()

    def add_entry(self, entry):
        """Add the given entry object to the appropriate data stores
        
        Arguments:
            entry {patientdata.PatientDataEntry} -- the entry object to be stored
        """

        # Add it to the main data store
        self.__data[entry.entry_id] = entry
        # If it contains spirometry data, add it to the SpiroData store
        if entry.get_spiro_data_entry() != None:
            self.__spiro_data.add_entry(entry)
        # If it contains pulse-oximetry data, add it to the PulseData store
        if entry.get_pulse_data_entry() != None:
            self.__pulse_data.add_entry(entry)
        # If it contains survey responses, add it to the SurveyData store
        if entry.get_survey_data_entry() != None:
            self.__survey_data.add_entry(entry)
        # Map its entry id to its entry date
        if entry.date in self.__date_to_indexes:
            self.__date_to_indexes[entry.date].append(entry.entry_id)
        else:
            self.__date_to_indexes[entry.date] = [entry.entry_id]

    def get_entry(self, date, entry_id):
        """Get the entry corresponding to the given date and entry ID, create it if it doesn't exist.
        
        Arguments:
            date {datetime.date} -- Date of the entry
            entry_id {str} -- The entry ID from the database
        
        Returns:
            patientdata.PatientDataEntry -- The corresponding entry object
        """

        if not entry_id in self.__data:
            new_data_entry = PatientDataEntry(date, entry_id)
            self.add_entry(new_data_entry)
        return self.__data[entry_id]

    def get_entry_by_id(self, entry_id):
        """Get the entry corresponding to the given entry ID
        
        Arguments:
            entry_id {str} -- the entry ID
        
        Raises:
            KeyError -- No entry exists for that entry ID
        
        Returns:
            patientdata.PatientDataEntry -- The corresponding entry object
        """

        if entry_id in self.__data:
            return self.__data[entry_id]
        else:
            raise KeyError(
                'StudyData: Unable to find entry for entry_id \"{0}\"'.format(entry_id))

    def get_entries_for_date(self, date):
        """Get all entries for the given date
        
        Arguments:
            date {datetime.date} -- The entry date
        
        Raises:
            KeyError -- No entries found for that date
        
        Returns:
            patientdata.PatientData -- The entries for that date
        """

        patient_data = PatientData()
        if date in self.__date_to_indexes:
            for entry_id in self.__date_to_indexes[date]:
                patient_data.add_entry(self.__data[entry_id])
            return patient_data
        else:
            raise KeyError(
                'StudyData: Unable to find entries for date \"{0}\"'.format(date))

    def get_entries_for_monitoring_month(self, monitoring_month):
        """Get all entries for the given month
        
        Arguments:
            monitoring_month {monitoringmonths.MonitoringMonth} -- The month
        
        Returns:
            patientdata.PatientData -- The entries for that month
        """

        patient_data = PatientData()
        for date in daterange(monitoring_month.start_date, monitoring_month.end_date + timedelta(days=1)):
            try:
                entries_for_date = self.get_entries_for_date(date)
                for entry in entries_for_date:
                    patient_data.add_entry(entries_for_date[entry])
            except Exception as e:
                # print e
                continue
        return patient_data

    def get_spiro_data(self):
        """Get the spirometry data store
        
        Returns:
            patientdata.SpiroData -- The spirometry data store
        """

        return self.__spiro_data

    def get_pulse_data(self):
        """Get the pulse-oximetry data store
        
        Returns:
            patientdata.PulseData -- The pulse-oximetry data store
        """

        return self.__pulse_data

    def get_survey_data(self):
        """Get the survey responses data store
        
        Returns:
            patientdata.SurveyData -- The survey responses data store
        """

        return self.__survey_data

    def __getitem__(self, key):
        return self.get_entry_by_id(key)

    def __iter__(self):
        return iter(self.__data)

    def __len__(self):
        return len(self.__data)


class Data:
    """The parent class for the three type-specific data store classes. Contains common methods for these classes.
    """

    def __init__(self):
        self.__data = []

    def add_entry(self, entry):
        """Add an entry to the entry list
        
        Arguments:
            entry {patientdata.PatientDataEntry} -- The entry to add
        """

        self.__data.append(entry)

    def get_entry_dates(self):
        """Get a sorted list of all the dates of the stored entries
        
        Returns:
            list(datetime.date) -- A sorted list of all the stored entries' dates
        """

        self.__data.sort()
        dates = []
        for entry in self.__data:
            dates.append(entry.date)
        return dates

    def get_data(self):
        """Get all the entries in the entry list
        
        Returns:
            list(patientdata.PatientDataEntry) -- The entry list
        """

        return self.__data

    def __iter__(self):
        self.__data.sort()
        return iter(self.__data)

    def __len__(self):
        return len(self.__data)

    def __str__(self):
        return "{0}".format([str(entry) for entry in self.__data])


class SpiroData(Data):
    """The data store for all entries containing spirometry data.
    """

    def __init__(self):
        """The SpiroData constructor
        """

        Data.__init__(self)
        self.__variances = []

    def add_entry(self, entry):
        """Add an entry containing spirometry data.
        
        Arguments:
            entry {patientdata.PatientDataEntry} -- The entry to add
        """
        # Add the entry in the main entry list for SpiroData
        Data.add_entry(self, entry)
        # If the entry contains a variance, create a new Variance object for the entry and add it to the list of variances
        if entry.get_spiro_data_entry().is_variance:
            self.__variances.append(Variance(entry))

        # If the entry is one of the four followup tests to a variance, try to add it to the most-recent Variance object
        try:
            if entry.get_spiro_data_entry().variance_counter != None:
                self.get_most_recent_variance().add_variance_test(entry)
        except:
            pass

    def get_most_recent_variance(self):
        """Get the Variance with the most-recent entry date
        
        Returns:
            patientdata.Variance -- The most-recent variance
        """

        self.__variances.sort()
        return self.__variances[-1]

    def get_fev1_max_list(self):
        """Get a list of the FEV1Max values for every entry, sorted by entry date
        
        Returns:
            list(float) -- A list of the FEV1Max values for all entries
        """

        self.get_data().sort()
        fev1_max_list = []
        for entry in self.get_data():
            fev1_max_list.append(entry.get_spiro_data_entry().fev1_max)
        return fev1_max_list

    def get_variances(self):
        """Get the list of the variances
        
        Returns:
            list(patientdata.Variance) -- A list of the variances
        """

        return self.__variances

    def __str__(self):
        return "SpiroData(data: {0}, variances: {1})".format(self.get_data(), [str(entry) for entry in self.__variances])


class PulseData(Data):
    """The data store for all entries containing pulse-oximetry data.
    """

    def __init__(self):
        """The PulseData constructor
        """

        Data.__init__(self)
        self.__abnormal = []

    def add_entry(self, entry):
        """Add an entry containing pulse-oximetry data
        
        Arguments:
            entry {patientdata.PatientDataEntry} -- The entry to add
        """

        # Add the entry to the main entry list for PulseData
        Data.add_entry(self, entry)
        # If either the heart rate or the oxygen-saturation levels are abnormal, add the entry to the list of abnormal entries
        if entry.get_pulse_data_entry().is_pulse_abnormal > 0 or entry.get_pulse_data_entry().is_o2sat_abnormal > 0:
            self.__abnormal.append(entry)

    def get_abnormal(self):
        """Get all entries with abnormal data, sorted by date
        
        Returns:
            list(patientdata.PatientDataEntry) -- The list of entries with abnormal data
        """

        self.__abnormal.sort()
        return self.__abnormal

    def __str__(self):
        return "PulseData(data: {0}, abnormal: {1})".format(self.get_data(), self.__abnormal)


class SurveyData(Data):
    """The data store for all entries containing survey response data.
    """

    def __init__(self):
        """The SurveyData constructor
        """

        Data.__init__(self)

    def add_entry(self, entry):
        """Add an entry containing survey response data
        
        Arguments:
            entry {patientdata.PatientDataEntry} -- The entry to add
        """

        Data.add_entry(self, entry)

    def get_summary(self):
        """Get an entry containing a summary of all the survey responses submitted. Think of this as all the questions from each survey ANDed together (i.e. entry 1 question 1 AND entry 2 question 1, etc.)
        
        Returns:
            patientdata.SurveyDataEntry -- The summarized survey responses
        """

        summary = SurveyDataEntry()
        for entry in self.get_data():
            survey_data_entry = entry.get_survey_data_entry()
            for question in survey_data_entry:
                if not question.id in summary:
                    summary.add_question(
                        question.id, question.answer, question.options)
                else:
                    summary[question.id].answer = summary[question.id].answer or question.answer
                    if question.answer:
                        for option in question.options:
                            summary[question.id].options.add(option)
        return summary


class PatientDataEntry:
    """This class contains all the information corresponding to one entry ID. It contains three sub-entries for spirometry data, pulse-oximetry data, and survey response data, which are each created as needed.
    """

    __spiro_data_entry = None
    __pulse_data_entry = None
    __survey_data_entry = None

    def __init__(self, date, entry_id):
        """The PatientDataEntry constructor
        
        Arguments:
            date {datetime.date} -- Date of the entry
            entry_id {str} -- The entry ID from the database
        """

        self.date = date
        self.entry_id = entry_id

    def set_spiro_data_entry(self, fev11, fev12, fev13, fev14, fev15, fev16, is_variance=False, variance_counter=None):
        """Add the spirometry data corresponding to this entry
        
        Arguments:
            fev11 {str} -- The first FEV1 data entry
            fev12 {str} -- The second FEV1 data entry
            fev13 {str} -- The third FEV1 data entry
            fev14 {str} -- The fourth FEV1 data entry
            fev15 {str} -- The fifth FEV1 data entry
            fev16 {str} -- The sixth FEV1 data entry
        
        Keyword Arguments:
            is_variance {bool} -- Whether there is a detected variance for the entry (default: {False})
            variance_counter {int} -- Whether this is one of the four followup tests after a variance is detected (a number between 1 and 4 inclusive), should only be set if is_variance=False (default: {None})
        """

        self.__spiro_data_entry = SpiroDataEntry(
            fev11, fev12, fev13, fev14, fev15, fev16, is_variance, variance_counter)

    def get_spiro_data_entry(self):
        """Get the spirometry data corresponding to this entry
        
        Returns:
            patientdata.SpiroDataEntry -- The spirometry data for this entry
        """

        return self.__spiro_data_entry

    def set_pulse_data_entry(self, minrate, maxrate, lowestsat, timeabnormal, timeminrate, is_pulse_abnormal, is_o2sat_abnormal):
        """Add the pulse-oximetry data corresponding to this entry
        
        Arguments:
            minrate {str} -- The minimum heartrate detected for the entry
            maxrate {str} -- The maximum heartrate detected for the entry
            lowestsat {str} -- The lowest oxygen saturation detected for the entry
            timeabnormal {str} -- [description]
            timeminrate {str} -- [description]
            is_pulse_abnormal {int} -- Whether the heartrate detected is abnormal (0 if False, 1 if True) 
            is_o2sat_abnormal {int} -- Whether the oxygen saturation detected is abnormal (0 if False, 1 if slightly abnormal, 2 if dangerously abnormal)
        """

        self.__pulse_data_entry = PulseDataEntry(
            minrate, maxrate, lowestsat, timeabnormal, timeminrate, is_pulse_abnormal, is_o2sat_abnormal)

    def get_pulse_data_entry(self):
        """Get the pulse-oximetry data corresponding to this entry
        
        Returns:
            patientdata.PulseDataEntry -- The pulse-oximetry data for this entry
        """

        return self.__pulse_data_entry

    def set_survey_data_entry(self):
        """Create the object to contain the survey response data corresponding to this entry. The actual survey response data should be added using add_survey_question or from within the object returned by get_survey_data_entry.
        """

        if self.__survey_data_entry == None:
            self.__survey_data_entry = SurveyDataEntry()

    def add_survey_question(self, question_id, question_answer, question_options):
        """Add the response to a survey question to the survey response data object corresponding to this entry
        
        Arguments:
            question_id {str} -- The id for the question (i.e. the first question would have a question_id of "q1")
            question_answer {str} -- The answer provided to the question (Yes if affirmative, anything else otherwise)
            question_options {list(str)} -- The list of responses to each symptom for the question. Either the name of the symptom if affirmative or "No such symptom" otherwise.
        """
        # create the SurveyDataEntry object if it doesn't already exist
        if self.__survey_data_entry == None:
            self.__survey_data_entry = SurveyDataEntry()
        # Add the question to the SurveyDataEntry object
        self.__survey_data_entry.add_question(
            question_id, question_answer, question_options)

    def get_survey_data_entry(self):
        """Get the survey response data corresponding to this entry
        
        Returns:
            patientdata.SurveyDataEntry -- The survey response data for this entry
        """

        return self.__survey_data_entry

    # These next two methods are provided to facilitate the Python sort functionality
    def __gt__(self, other):
        if self.date != other.date:
            return self.date > other.date
        else:
            return self.entry_id > other.entry_id

    def __lt__(self, other):
        if self.date != other.date:
            return self.date < other.date
        else:
            return self.entry_id < other.entry_id

    def __str__(self):
        return "PatientDataEntry({0},{1},{2},{3},{4})".format(
            self.date, self.entry_id, self.get_spiro_data_entry(), self.get_pulse_data_entry(), self.get_survey_data_entry())


class Variance:
    """A single variance entry and its corresponding followup test entries
    """

    def __init__(self, entry):
        """The Variance constructor
        
        Arguments:
            entry {patientdata.PatientDataEntry} -- The entry containing the detected variance
        """

        self.variance = entry
        self.variance_tests = []

    def add_variance_test(self, entry):
        """Add a followup test entry
        
        Arguments:
            entry {patientdata.PatientDataEntry} -- the followup test entry
        """
        # Add the entry to the list of followup tests
        self.variance_tests.append(entry)

    # Some methods to facilitate the sorting of the Variance objects based on the date of the variance entry
    def __gt__(self, other):
        return self.variance > other.variance

    def __lt__(self, other):
        return self.variance < other.variance

    def __str__(self):
        return "Variance({0},{1})".format(self.variance, [str(variance_test) for variance_test in self.variance_tests])


class SpiroDataEntry:
    """All the data corresponding to an individual spirometry data entry
    """

    def __init__(self, fev11, fev12, fev13, fev14, fev15, fev16, is_variance=False, variance_counter=None):
        """The SpiroDataEntry constructor
        
        Arguments:
            fev11 {str} -- The first FEV1 data entry
            fev12 {str} -- The second FEV1 data entry
            fev13 {str} -- The third FEV1 data entry
            fev14 {str} -- The fourth FEV1 data entry
            fev15 {str} -- The fifth FEV1 data entry
            fev16 {str} -- The sixth FEV1 data entry
        
        Keyword Arguments:
            is_variance {bool} -- Whether there is a detected variance for the entry (default: {False})
            variance_counter {int} -- Whether this is one of the four followup tests after a variance is detected (a number between 1 and 4 inclusive), should only be set if is_variance=False (default: {None})
        """
        # Store the FEV1 data
        self.fev1_data = (fev11, fev12, fev13, fev14, fev15, fev16)
        # Store whether the entry is a variance
        self.is_variance = False if is_variance == None else is_variance
        # If it's a followup test to a variance, record the number of the followup test
        self.variance_counter = None if is_variance else variance_counter
        # Calculate the FEV1Max value using the FEV1 data
        self.fev1_max = max(self.fev1_data)

    def __str__(self):
        return "SpiroDataEntry({0},{1},{2})".format(self.fev1_max, self.is_variance, self.variance_counter)


class PulseDataEntry:
    """All the data corresponding to an individual pulse-oximetry data entry
    """

    def __init__(self, minrate, maxrate, lowestsat, timeabnormal, timeminrate, is_pulse_abnormal, is_o2sat_abnormal):
        """The PulseDataEntry constructor
        
        Arguments:
            minrate {str} -- The minimum heartrate detected for the entry
            maxrate {str} -- The maximum heartrate detected for the entry
            lowestsat {str} -- The lowest oxygen saturation detected for the entry
            timeabnormal {str} -- [description]
            timeminrate {str} -- [description]
            is_pulse_abnormal {int} -- Whether the heartrate detected is abnormal (0 if False, 1 if True) 
            is_o2sat_abnormal {int} -- Whether the oxygen saturation detected is abnormal (0 if False, 1 if slightly abnormal, 2 if dangerously abnormal)
        """

        self.minrate = minrate
        self.maxrate = maxrate
        self.lowestsat = lowestsat
        self.timeabnormal = timeabnormal
        self.timeminrate = timeminrate
        self.is_pulse_abnormal = is_pulse_abnormal
        self.is_o2sat_abnormal = is_o2sat_abnormal

    def __str__(self):
        return "PulseDataEntry({0},{1},{2},{3},{4})".format(
            self.minrate,
            self.maxrate,
            self.lowestsat,
            self.is_pulse_abnormal,
            self.is_o2sat_abnormal)


class SurveyDataEntry:
    """All the data corresponding to an individual survey response data entry
    """

    def __init__(self):
        """The SurveyDataEntry constructor
        """

        self.__questions = []
        self.__id_to_question_map = {}

    def add_question(self, question_id, question_answer, question_options):
        """Add the response to a survey question
        
        Arguments:
            question_id {str} -- The id for the question (i.e. the first question would have a question_id of "q1")
            question_answer {str} -- The answer provided to the question (Yes if affirmative, anything else otherwise)
            question_options {list(str)} -- The list of responses to each symptom for the question. Either the name of the symptom if affirmative or "No such symptom" otherwise.
        """

        new_question = SurveyQuestion(
            question_id, question_answer, question_options)
        self.__questions.append(new_question)
        self.__id_to_question_map[question_id] = new_question

    def get_questions(self):
        """Get all question responses for the current survey response
        
        Returns:
            list(patientdata.SurveyQuestion) -- A list of the question responses
        """

        return self.__questions

    def __iter__(self):
        return iter(self.__questions)

    def __getitem__(self, key):
        """Get a survey question response given the question id
        
        Arguments:
            key {str} -- The question id
        
        Raises:
            KeyError -- No question with that ID could be found
        
        Returns:
            patientdata.SurveyQuestion -- The corresponding survey question response
        """

        if key in self.__id_to_question_map:
            return self.__id_to_question_map[key]
        else:
            raise KeyError(
                'SurveyDataEntry: Unable to find question for question_id \"{0}\"'.format(key))

    def __contains__(self, key):
        """Check if a question response with the given question ID exists in the survey response data
        
        Arguments:
            key {str} -- The question id
        
        Returns:
            bool -- True if it exists, False otherwise
        """

        return key in self.__id_to_question_map

    def __str__(self):
        return "SurveyDataEntry({0})".format([str(question) for question in self.__questions])


class SurveyQuestion:
    """A survey question response
    """

    def __init__(self, question_id, question_answer, question_options):
        """The SurveyQuestion constructor
        
        Arguments:
            question_id {str} -- The id for the question (i.e. the first question would have a question_id of "q1")
            question_answer {str} -- The answer provided to the question (Yes if affirmative, anything else otherwise)
            question_options {list(str)} -- The list of responses to each symptom for the question. Either the name of the symptom if affirmative or "No such symptom" otherwise.
        """

        self.id = question_id
        # Get the value of the question answer
        if isinstance(question_answer, bool):
            self.answer = question_answer
        else:
            self.answer = True if str(
                question_answer).lower() == "Yes".lower() else False
        self.options = set()
        # If the question is answered affirmatively, store all the options for which the response is not "No such symptom"
        if self.answer:
            for option in question_options:
                if str(option).lower() != "No such symptom".lower():
                    self.options.add(str(option))

    def get_response_str(self):
        return ", ".join(self.options) if self.answer else "No such symptom"

    def __str__(self):
        return "{0}: [{1}, {2}]".format(self.id, self.answer, str(self.options))
