# Hush

Hush is an open-source app for Android that blocks incoming phone calls from numbers that are not saved in the user's contacts. 

This app does not replace the default phone app. It requires only the essential permissions and keep your data only in your phone.

## Requirements

- Android 10 or later.

## Permissions

| Permission | Required | Why |
| --- | --- | --- |
| **Call screening role** | Yes | Lets Hush check each incoming call before the phone rings and reject it. |
| **Contacts** (`READ_CONTACTS`) | Yes | Lets Hush check whether the caller's number is saved in the contacts. Contacts are only read, never changed or copied. |
| **Notifications** (`POST_NOTIFICATIONS`) | No | Needed on Android 13 and later to show notifications about blocked calls. Hush only asks for it if the user turns notifications on. |

## Setup

The first time the user opens Hush, a setup wizard guides them through these steps:

1. **Welcome**: a short description of what Hush does and a **Get started** button.
2. **Permissions**: explains why Hush needs the call screening role and contacts access. An **Allow** button opens the Android permission dialogs. The wizard only moves on once both required permissions are granted. If the user denies one, they can tap **Allow** again. If Android stops showing a dialog, the button opens the matching system settings screen instead (default apps for the call screening role, app info for contacts).
3. **Notifications**: the user picks one of the notification options (see [Notifications](#notifications)), with **Every blocked call** already selected. If they pick an option other than **Never**, Hush asks for the Android notification permission when they tap **Finish**. Denying it does not block the setup. Hush just works as if **Never** was selected.
4. **Done**: the app opens on the home screen, with call blocking turned on.

## Features

### Call blocking

A switch that turns blocking on or off. It is **on** by default.

- **On**: incoming calls from numbers that are not in the contacts or in the allowlist are rejected before the phone rings, and so are calls from hidden or private numbers, which can never match a contact. They do not show the system missed-call notification. Android still lists them in the system call log, marked as blocked: it does that for every call rejected by a screening app, and apps cannot turn it off. Each blocked call is saved in Hush's history.
- **Off**: Hush allows every call. Nothing is blocked and nothing is saved in the history.

The switch only works while Hush has the permissions it needs (contacts access and the call screening role). If a permission is missing, the app shows it and asks the user to grant it again.

### History

A list of every blocked call, newest first. Each entry shows the number and the date and time of the call.

- Selecting an entry lets the user add that number to the allowlist.
- The user can clear the whole history.

### Statistics

A counter with the number of calls blocked in the last 7 days.

## Settings

### Allowlist

A list of numbers that are never blocked, even if they are not saved in the contacts. Useful for numbers the user wants to receive calls from without adding them as contacts. The list is empty by default.

- The user adds a number by typing it, with an optional name to identify it. A number can also be added straight from the history, by selecting a blocked call.

### Notifications

Sets how Hush tells the user about blocked calls. The user picks one option:

| Option | Behavior |
| --- | --- |
| **Every blocked call** (default) | One notification for each blocked call, with the number and the time. |
| **Daily summary**  | One notification per day at around 8 PM, with the number of calls blocked since the previous summary. No notification if no calls were blocked. Android may delay it a little to save battery. |
| **Never** | No notifications. Blocked calls only appear in the app. |

On Android 13 and later, Hush asks for the notification permission when the user picks an option other than **Never**. If the permission is denied, Hush works as if **Never** was selected.
