// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public final class FindMeetingQuery {

  /**
   * This function returns a list of potential time ranges for a meeting 
   * during the day. The request contains the attendees and the duration
   * of the meeting, and events contains the other events that are 
   * scheduled already. If an attendee of the meeting request is an
   * attendee of another event, the times that the other event covers
   * are no longer possible for the meeting.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> possibleTimes = new ArrayList<TimeRange>();
    
    // If the request is over a day, then there are no time slots available
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
        return possibleTimes;
    }

    // Start with the whole day available
    possibleTimes.add(TimeRange.WHOLE_DAY);

    if (request.getAttendees().size() == 0) {
        return possibleTimes;
    }

    // Only update times for events that have attendees from the meeting request
    for (Event event : events) {
        for (String attendee : request.getAttendees()) {
            if (event.getAttendees().contains(attendee)) {
                updatePossibleTimes(possibleTimes, event.getWhen(), request.getDuration());
            }
        }
    }

    return possibleTimes;
  }

  /** 
   * Update possible meeting times by excluding time ranges of an event
   */
  private void updatePossibleTimes(Collection<TimeRange> possibleTimes, TimeRange eventTime, long requestDuration) {
    Collection<TimeRange> rangesToModify = new ArrayList<TimeRange>();

    // Check for time ranges that contain the event in question
    for (TimeRange timeSlot : possibleTimes) {
        if (timeSlot.contains(eventTime.start()) || timeSlot.contains(eventTime.end())) {
            rangesToModify.add(timeSlot);
        }
    }

    splitTimeRanges(possibleTimes, rangesToModify, eventTime);
    removeInsufficientSlots(possibleTimes, requestDuration);

  }

  /** 
   * For ranges that overlap the event, remove the invalid parts from the range
   */
  private void splitTimeRanges(Collection<TimeRange> possibleTimes, Collection<TimeRange> rangesToModify, TimeRange eventTime) {
    for (TimeRange timeSlot : rangesToModify) {
        if (timeSlot.contains(eventTime.start())) {
            possibleTimes.add(TimeRange.fromStartEnd(timeSlot.start(), eventTime.start(), false));
        }

        if (timeSlot.contains(eventTime.end())) {
            possibleTimes.add(TimeRange.fromStartEnd(eventTime.end(), timeSlot.end(), false));
        }

        possibleTimes.remove(timeSlot);
    }
  }

  /** 
   * Remove possible meeting times that are not long enough for the requested meeting
   */
  private void removeInsufficientSlots(Collection<TimeRange> possibleTimes, long requestDuration) {
      Collection<TimeRange> rangesToRemove = new ArrayList<TimeRange>();
      for (TimeRange range : possibleTimes) {
          if (range.duration() < requestDuration) {
              rangesToRemove.add(range);
          }
      }

      possibleTimes.removeAll(rangesToRemove);
  }
}
