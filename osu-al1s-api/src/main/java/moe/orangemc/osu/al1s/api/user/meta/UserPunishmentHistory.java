/*
 * Copyright 2024 Astro angelfish
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package moe.orangemc.osu.al1s.api.user.meta;

import moe.orangemc.osu.al1s.api.user.User;

/**
 * Records one punishment entry of a user.
 * @param receiver the {@link User} being punished
 * @param description the reason for punishing
 * @param id punishment ID
 * @param length time duration during which the punishment keeps valid
 * @param permanent whether the punishment is permanent with this user
 * @param awardTime the time when the user got this record
 * @param type the {@link PunishmentType} of this record
 */
public record UserPunishmentHistory(User receiver, String description, int id, int length, boolean permanent, long awardTime, PunishmentType type) {
}
