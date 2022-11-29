/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.safetycenter;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.annotation.NonNull;
import android.util.ArrayMap;
import android.util.ArraySet;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.safetycenter.internaldata.SafetyCenterIssueKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/** Deduplicates issues based on deduplication info provided by the source and the issue. */
@RequiresApi(UPSIDE_DOWN_CAKE)
final class SafetyCenterIssueDeduplicator {

    /**
     * Accepts a list of issues sorted by priority and filters out duplicates.
     *
     * <p>Issues are considered duplicate if they have the same deduplication id and were sent by
     * sources which are part of the same deduplication group. All but the highest priority
     * duplicate issue will be filtered out.
     *
     * <p>This method modifies the given argument.
     */
    void deduplicateIssues(@NonNull List<SafetyCenterIssueExtended> sortedIssues) {
        // (dedup key) -> list(issues)
        ArrayMap<DeduplicationKey, List<SafetyCenterIssueExtended>> dedupBuckets =
                createDedupBuckets(sortedIssues);

        ArraySet<SafetyCenterIssueKey> duplicatesToFilterOut =
                getDuplicatesToFilterOut(dedupBuckets);

        Iterator<SafetyCenterIssueExtended> it = sortedIssues.iterator();
        while (it.hasNext()) {
            if (duplicatesToFilterOut.contains(it.next().getSafetyCenterIssueKey())) {
                it.remove();
            }
        }
    }

    /** Returns a set of duplicate issues that need to be filtered out. */
    @NonNull
    private static ArraySet<SafetyCenterIssueKey> getDuplicatesToFilterOut(
            @NonNull ArrayMap<DeduplicationKey, List<SafetyCenterIssueExtended>> dedupBuckets) {
        ArraySet<SafetyCenterIssueKey> duplicatesToFilterOut = new ArraySet<>();

        for (int i = 0; i < dedupBuckets.size(); i++) {
            List<SafetyCenterIssueExtended> duplicates = dedupBuckets.valueAt(i);
            // all but the top one in the bucket
            for (int j = 1; j < duplicates.size(); j++) {
                duplicatesToFilterOut.add(duplicates.get(j).getSafetyCenterIssueKey());
            }
        }

        return duplicatesToFilterOut;
    }

    /** Returns a mapping (dedup key) -> list(issues). */
    @NonNull
    private static ArrayMap<DeduplicationKey, List<SafetyCenterIssueExtended>> createDedupBuckets(
            @NonNull List<SafetyCenterIssueExtended> sortedIssues) {
        ArrayMap<DeduplicationKey, List<SafetyCenterIssueExtended>> dedupBuckets = new ArrayMap<>();

        for (int i = 0; i < sortedIssues.size(); i++) {
            SafetyCenterIssueExtended issue = sortedIssues.get(i);
            DeduplicationKey dedupKey = getDedupKey(issue);
            if (dedupKey == null) {
                continue;
            }

            // each bucket will remain sorted
            List<SafetyCenterIssueExtended> bucket =
                    dedupBuckets.getOrDefault(dedupKey, new ArrayList<>());
            bucket.add(issue);

            dedupBuckets.put(dedupKey, bucket);
        }

        return dedupBuckets;
    }

    /** Returns deduplication key of the given issue. */
    @Nullable
    private static DeduplicationKey getDedupKey(@NonNull SafetyCenterIssueExtended issue) {
        if (issue.getDeduplicationGroup() == null || issue.getDeduplicationId() == null) {
            return null;
        }
        return new DeduplicationKey(issue.getDeduplicationGroup(), issue.getDeduplicationId());
    }

    private static class DeduplicationKey {

        @NonNull private final String mDeduplicationGroup;
        @NonNull private final String mDeduplicationId;

        private DeduplicationKey(
                @NonNull String deduplicationGroup, @NonNull String deduplicationId) {
            mDeduplicationGroup = deduplicationGroup;
            mDeduplicationId = deduplicationId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mDeduplicationGroup, mDeduplicationId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DeduplicationKey)) return false;
            DeduplicationKey dedupKey = (DeduplicationKey) o;
            return mDeduplicationGroup.equals(dedupKey.mDeduplicationGroup)
                    && mDeduplicationId.equals(dedupKey.mDeduplicationId);
        }
    }
}
