import argparse
from enum import Enum
import sys
import os
import csv
import itertools
import functools
import re

class Format(Enum):
    CSV = "csv"
    TEX = "tex"

class BugPatternStatus(Enum):
    VALIDATED = "validated"
    FAILED = "failed"
    NOT_VALIDATED = "not_validated"
    NOT_FOUND = "-"
    NOT_LOADED = "not_loaded"

class BugPatternValidationStatistics:
    def __init__(self, status, inputs, resets, time=None):
        self.status = status
        self.inputs = inputs
        self.resets = resets

    def get_status(self):
        return self.status

    def get_inputs(self):
        return self.inputs

    def get_resets(self):
        return self.resets

    def get_time(self):
        return self.time

    def __repr__(self):
        return "BugPatternValidationStatistics(Status:{}, Inputs:{}, Resets:{})".format(str(self.status), str(self.inputs), str(self.resets))

class ValidationStatistics:
    def __init__(self, bp_validation_stats, validation_time):
        self._bp_validation_stats = bp_validation_stats
        self._total_time = validation_time

    def get_total_time(self):
        return self._total_time

    def get_total_inputs(self):
        return sum([stats.get_inputs() for stats in self._bp_validation_stats])

    def get_total_resets(self):
        return sum([stats.get_resets() for stats in self._bp_validation_stats])

    def get_total_validating_inputs(self):
        return sum([stats.get_inputs() for stats in self._bp_validation_stats if stats.get_status() == BugPatternStatus.VALIDATED])

    def get_total_validating_resets(self):
        return sum([stats.get_resets() for stats in self._bp_validation_stats if stats.get_status() == BugPatternStatus.VALIDATED])
    
class ResultData:

    def __init__(self, exp_name, validation, bugpatterns, exp_stats=None, bp_stats=None):
        self._exp_name = exp_name
        self._validation = validation
        self._bugpatterns = bugpatterns
        self._bugpattern_stats = bp_stats
        self._exp_stats = exp_stats

    def get_status_for_bugpattern(self, bp_name):
        # this check is out-of-place here but w/e
        if bp_name in self._bugpatterns:
            return self._bugpatterns[bp_name]
        else:
            return BugPatternStatus.NOT_LOADED

    def get_stats_for_bugpattern(self, bp_name):
        return None if self._bugpattern_stats is None else self._bugpattern_stats[bp_name] if bp_name in self._bugpattern_stats else None

    def get_bugpattern_statuses(self):
        return self._bugpatterns

    def get_bugpattern_names(self):
        return list(self._bugpatterns)

    def get_bugpattern_stats(self):
        return self._bugpattern_stats

    def count_found_bugs(self):
        return sum(self._bugpatterns[bp] != BugPatternStatus.NOT_FOUND  for bp in self._bugpattern_stats)

    def count_validated_bugs(self):
        result = sum(self._bugpatterns[bp] == BugPatternStatus.VALIDATED  for bp in self._bugpattern_stats)
        return result

    def get_exp_name(self):
        return self._exp_name

    def get_exp_stats(self):
        return self._exp_stats

    def is_validation_done(self):
        return self._validation is True

    def is_empty(self):
        return len(self._bugpatterns)

    def __repr__(self):
        return "ResultData(ExpName:{}, ValidationEnabled:{}, BugPatternStatus:{})".format(self._exp_name, str(self._validation), str(self._bugpatterns))


# TODO writerow can be an abstract method and used in the main class. In any case, collate is not a good name.
class Collator:
    def collate_results(self, result_datum, output_file):
        raise NotImplementedError

    def bugpattern_status_string(self, bp_name, result_data):
        desc = str(result_data.get_status_for_bugpattern(bp_name).value)
        if result_data.is_validation_done() and result_data.get_stats_for_bugpattern(bp_name) is not None:
            bp_stats = result_data.get_stats_for_bugpattern(bp_name)
            desc = desc + "(inp: {}, rst: {})".format(bp_stats.inputs, bp_stats.resets)
        return desc 

class TexCollator(Collator):
    def collate_results(self, result_datum, output_file):
        raise NotImplementedError

class CsvCollator(Collator):    
    def collate_results(self, result_datum, output_file):
        nprint = lambda x : str(x) if x is not None else ""
        with open(output_file, 'w', newline='') as csvfile:
            csv_writer = csv.writer(csvfile)
            if len(result_datum) > 0:
                bp_names = result_datum[0].get_bugpattern_names()
                suts_bp = dict( [(result_data.get_exp_name(), result_data) for result_data in result_datum])
                suts = list(suts_bp)
                sorted_suts = sorted(suts)
                csv_writer.writerow(["Bug\SUT"] + sorted_suts)

                for bp_name in sorted(bp_names):
                    csv_writer.writerow([bp_name] + [self.bugpattern_status_string(bp_name, suts_bp[sut]) for sut in sorted_suts])
                   
                include_validation=any([result_data.is_validation_done() and result_data.get_exp_stats() is not None for result_data in result_datum])

                if include_validation:
                    csv_writer.writerow(["-" for _ in range(len(suts)+1)])
                    csv_writer.writerow(["-" for _ in range(len(suts)+1)])

                    # the rows
                    bugs_found = list()
                    bugs_validated = list()
                    total_inputs = list()
                    total_resets = list()
                    total_time = list()
                    total_validating_inputs = list()
                    total_validating_resets = list()
                    total_time = list()
                    for sut in sorted_suts:
                        sut_exp = suts_bp[sut]
                        bugs_found.append(sut_exp.count_found_bugs())
                        bugs_validated.append(sut_exp.count_validated_bugs())
                        total_inputs.append(sut_exp.get_exp_stats().get_total_inputs() if sut_exp.get_exp_stats() is not None else "")
                        total_resets.append(sut_exp.get_exp_stats().get_total_resets() if sut_exp.get_exp_stats() is not None else "")
                        total_validating_resets.append(sut_exp.get_exp_stats().get_total_validating_resets() if sut_exp.get_exp_stats() is not None else "")
                        total_validating_inputs.append(sut_exp.get_exp_stats().get_total_validating_inputs() if sut_exp.get_exp_stats() is not None else "")
                        total_time.append(sut_exp.get_exp_stats().get_total_time() if sut_exp.get_exp_stats() is not None else "")

                    csv_writer.writerow(["Bugs Found in Model"] + bugs_found)
                    csv_writer.writerow(["Bugs Validated"] + bugs_validated)
                    csv_writer.writerow(["Total Inputs"] + total_inputs)
                    csv_writer.writerow(["Total Resets"] + total_resets)
                    csv_writer.writerow(["Total Inputs for Validated Bugs"] + total_validating_inputs)
                    csv_writer.writerow(["Total Resets for Validated Bugs"] + total_validating_resets)
                    csv_writer.writerow(["Total Time(ms)"] + total_time)
                    

                    #csv_writer.writerow(["Total Inputs"] + [suts_bp[sut].get_exp_stats().get_total_inputs() if suts_bp[sut].get_exp_stats() is not None else "" for sut in sorted_suts] )
                    #csv_writer.writerow(["Total Resets"] + [suts_bp[sut].get_exp_stats().get_total_resets() if suts_bp[sut].get_exp_stats() is not None else "" for sut in sorted_suts] )
                    #csv_writer.writerow(["Total Time(ms)"] + [suts_bp[sut].get_exp_stats().get_total_time() if suts_bp[sut].get_exp_stats() is not None else "" for sut in sorted_suts] )

def get_result_file(result_dir):
    return os.path.join(result_dir, "bug_report.txt")

def is_result_dir(dir):
    return os.path.isfile(get_result_file(dir))

def get_result_files(dir):
    if is_result_dir(dir):
        return [get_result_file(dir)]
    elif os.path.isdir(dir):
        return [get_result_file(os.path.join(dir, child_dir)) for child_dir in os.listdir(dir) if is_result_dir(os.path.join(dir, child_dir))] 

class ParsingException(Exception):
    def __init__(self,  message):
        self.message = message


def get_matching_line(regex, lines, from_index=None, match_expected=True):
    if from_index is None:
        from_index = 0
    crt_index = from_index
    for line in lines[from_index:]:
        match = re.search(regex, line)
        crt_index += 1
        if match:
            return (line, crt_index)
    if  match_expected:
        raise ParsingException("Could not find a match for {} in bug report.".format(regex))

def get_matching_lines_in_order(regexes, lines, from_index=None):
    idx = from_index if from_index is not None else 0
    matching_lines=[]
    for regex in regexes:
        (line, idx) = get_matching_line(regex, lines, from_index=idx)
        matching_lines.append(line)
    return (matching_lines, idx)

def read_lines(result_file):        
    lines = None
    with open(result_file) as f:
        lines = f.readlines()
    return lines

def read_list_from_line(line):
    return [l.strip() for l in line[line.index("[")+1 : line.index("]")].split(",") if len(l.strip()) > 0]

def read_number(line):
    return int(line.split(":")[1].strip())

def get_result_data(result_file):
    validation = None
    bugpatterns = dict()
    #bugpatterns["Uncategorized"] = BugPatternStatus.NOT_FOUND
    
    lines = read_lines(result_file) 
    (line, idx) = get_matching_line("Bug Validation Enabled", lines)
    if "true" in line:
        validation = True
    else:
        validation = False

    stats = None
    bugpattern_stats = None
    total_time = None
    
    if validation:
        (line, idx) = get_matching_line("Time bug-checking took", lines, from_index=idx)
        total_time = read_number(line)

    (line, idx) = get_matching_line("Bug patterns loaded", lines, from_index=idx)
    for loadedbp in read_list_from_line(line):
        bugpatterns[loadedbp] = BugPatternStatus.NOT_FOUND

    (line, idx) = get_matching_line("Bug patterns found", lines, from_index=idx)
    for foundbp in read_list_from_line(line):
        bugpatterns[foundbp] = BugPatternStatus.NOT_VALIDATED

    if validation:
        #(line, idx) = get_matching_line("Time bug-checking", lines, from_index=idx)
        (line, idx) = get_matching_line("Bug patterns validated", lines, from_index=idx)
        validatedbps = read_list_from_line(line)
        for bp in list(bugpatterns):
            if bugpatterns[bp] is BugPatternStatus.NOT_VALIDATED and bp != "Uncategorized":
                bugpatterns[bp] = BugPatternStatus.VALIDATED if bp in validatedbps else BugPatternStatus.FAILED

        found_bp_list = [bugpattern for bugpattern in list(bugpatterns) if bugpatterns[bugpattern] != BugPatternStatus.NOT_FOUND and bugpattern != "Uncategorized" and bugpattern != "Handshake Bug"]

        (_, idx) = get_matching_line("Inputs per Bug", lines, from_index=idx)
        bugpattern_stats = dict()
        (bug_inputs, idx) = get_matching_lines_in_order(found_bp_list, lines, from_index=idx)

        (_, idx) = get_matching_line("Resets per Bug", lines, from_index=idx)
        (bug_resets, idx) = get_matching_lines_in_order(found_bp_list, lines, from_index=idx)

        for (bp_name, bug_inputs, bug_resets) in zip(found_bp_list, bug_inputs, bug_resets):
            bugpattern_stats[bp_name] = BugPatternValidationStatistics(bugpatterns[bp_name], read_number(bug_inputs), read_number(bug_resets))
        
        stats = ValidationStatistics(list(bugpattern_stats.values()), total_time)

    return ResultData(os.path.basename(os.path.dirname(result_file)), validation, bugpatterns, exp_stats=stats, bp_stats=bugpattern_stats)

def get_collator(format):
    if format == Format.CSV.value:
        return CsvCollator()
    elif format == Format.TEX.value:
        return TexCollator()
    else:
        raise NotImplementedError

def coleasce_status(s1, s2):
    prio_list = [BugPatternStatus.NOT_LOADED, BugPatternStatus.NOT_FOUND, BugPatternStatus.NOT_VALIDATED, BugPatternStatus.FAILED, BugPatternStatus.VALIDATED]
    return s1 if prio_list.index(s1) > prio_list.index(s2) else s2

class CoalesceException(Exception):
    def __init__(self,  message):
        self.message = message

def coalesce_bp(name, bp1, bp2):
    if bp1.is_validation_done() is not bp2.is_validation_done():
        raise CoalesceException("Cannot coalesce {} and {} due to different validation statuses: {} and {}".format(bp1.get_exp_name(), bp2.get_exp_name(), bp1.is_validation_done(), bp2.is_validation_done()))
    if set(bp1.get_bugpattern_names()) != set(bp2.get_bugpattern_names()):
        raise CoalesceException("Cannot coalesce {} and {} due to different bug patterns: {} and {}".format(bp1.get_exp_name(), bp2.get_exp_name(), str(bp1.get_bugpattern_names()), str(bp2.get_bugpattern_names())))
    bugpatterns = dict()
    for bp_name in bp1.get_bugpattern_names():
        s1 = bp1.get_status_for_bugpattern(bp_name)
        s2 = bp2.get_status_for_bugpattern(bp_name)
        new_status = coleasce_status(s1, s2)
        bugpatterns[bp_name] = new_status

    return ResultData(name, bp1.is_validation_done(), bugpatterns)

def coalesce_results(result_datum):
    coalesced_results = []
    get_sut = lambda result: result.get_exp_name()[0:result.get_exp_name().find("_")]
    sorted_results = sorted(result_datum, key=get_sut)
    for sut, results_iter in itertools.groupby(sorted_results, get_sut):
        results = list(results_iter)
        if len(results) == 0:
            continue
        
        initial = ResultData(get_sut(results[0]), results[0].is_validation_done(), results[0].get_bugpattern_statuses(), bp_stats=results[0].get_bugpattern_stats(), exp_stats=results[0].get_exp_stats())
        
        # if there is only one experiment per SUT, we do not coalesce, just rename the experiment to the name of the SUT
        if len(results) > 1: 
            coalesced = functools.reduce(lambda bp1, bp2, sut_name=sut: coalesce_bp(sut_name, bp1, bp2), results, initial)
            coalesced_results.append(coalesced)
        else:
            coalesced_results.append(initial)
    
    return coalesced_results

def collate_results(result_datum, format, output_name):
    collator = get_collator(format)
    collator.collate_results(result_datum, output_name)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Utility for collating bug-checker results into a table, providing a summary.')
    parser.add_argument("-f","--format", type=str, default=Format.CSV.value, choices=[Format.CSV.value, Format.TEX.value], help="The format of the resulting table.")
    parser.add_argument("-o","--output", type=str, help="The name of the file in which the resulting table is stored.")
    parser.add_argument("-c","--coalesce", action='store_true', help="Coalesce results obtained for same SUTs. Loses statistics.")
    parser.add_argument("results", type=str, help="an experiment results directory or a directory containing experimental result directories.", nargs='+')
    args = parser.parse_args()

    if len(sys.argv) == 1:
        parser.print_usage()
        exit(0)
    
    if args.output is None:
        output = "output." + str(args.format)
    else:
        output = args.output
    
    result_files = [] 
    for results_dir in args.results:
        dir_result_files = get_result_files(results_dir)
        if dir_result_files is None:
            print("No result files found in ", results_dir)
            continue
        result_files.extend(get_result_files(results_dir))
    
    if len(result_files) > 0:
        result_datum = []
        for  result_file in result_files: 
            try:
                result_datum.append(get_result_data(result_file))
            except:
                print("Exception processing result file", result_file)
                raise
        final_resuls = None

        if args.coalesce:
            final_results = coalesce_results(result_datum)
        else:
            final_results = result_datum

        collate_results(final_results, args.format, output)


