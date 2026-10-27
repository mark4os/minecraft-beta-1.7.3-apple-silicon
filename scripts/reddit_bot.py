#!/usr/bin/env python3
"""
Reddit Automation Helper for Minecraft Beta 1.7.3 Apple Silicon Port.
Uses PRAW (Python Reddit API Wrapper) with local credentials.
"""

import os
import sys
import argparse
from pathlib import Path

try:
    import praw
    from dotenv import load_dotenv
except ImportError:
    print("Error: praw or python-dotenv not installed. Run: pip install praw python-dotenv")
    sys.exit(1)

ENV_FILE = Path(__file__).resolve().parent / ".reddit.env"
if not ENV_FILE.exists():
    ENV_FILE = Path(__file__).resolve().parent.parent / ".reddit.env"

if ENV_FILE.exists():
    load_dotenv(ENV_FILE)

def get_reddit_client():
    client_id = os.getenv("REDDIT_CLIENT_ID")
    client_secret = os.getenv("REDDIT_CLIENT_SECRET")
    username = os.getenv("REDDIT_USERNAME")
    password = os.getenv("REDDIT_PASSWORD")
    user_agent = os.getenv("REDDIT_USER_AGENT", f"macOS:mc-b173-port:v1.0 (by /u/{username})")

    if not all([client_id, client_secret, username, password]):
        print(f"Error: Missing credentials in {ENV_FILE}")
        print("Required: REDDIT_CLIENT_ID, REDDIT_CLIENT_SECRET, REDDIT_USERNAME, REDDIT_PASSWORD")
        sys.exit(1)

    return praw.Reddit(
        client_id=client_id,
        client_secret=client_secret,
        username=username,
        password=password,
        user_agent=user_agent
    )

def cmd_status():
    reddit = get_reddit_client()
    user = reddit.user.me()
    print(f"Successfully authenticated as: /u/{user.name}")
    print(f"Comment Karma: {user.comment_karma} | Link Karma: {user.link_karma}")

def cmd_list_posts():
    reddit = get_reddit_client()
    user = reddit.user.me()
    print(f"Recent submissions by /u/{user.name}:")
    for submission in user.submissions.new(limit=5):
        print(f"[{submission.id}] ({submission.subreddit.display_name}) {submission.title}")
        print(f"     Upvotes: {submission.score} | Comments: {submission.num_comments} | URL: {submission.url}")

def cmd_get_comments(post_id):
    reddit = get_reddit_client()
    submission = reddit.submission(id=post_id)
    print(f"Comments for: '{submission.title}' [{post_id}]")
    submission.comments.replace_more(limit=0)
    for comment in submission.comments.list():
        author = comment.author.name if comment.author else "[deleted]"
        print(f"\n--- Comment [{comment.id}] by /u/{author} ({comment.score} pts) ---")
        print(comment.body)

def cmd_reply(comment_id, text):
    reddit = get_reddit_client()
    comment = reddit.comment(id=comment_id)
    reply = comment.reply(text)
    print(f"Successfully replied to comment {comment_id}! Reply ID: {reply.id}")

def cmd_comment_post(post_id, text):
    reddit = get_reddit_client()
    submission = reddit.submission(id=post_id)
    comment = submission.reply(text)
    print(f"Successfully added comment to post {post_id}! Comment ID: {comment.id}")

def main():
    parser = argparse.ArgumentParser(description="Reddit CLI helper for Minecraft port")
    subparsers = parser.add_subparsers(dest="command")

    subparsers.add_parser("status", help="Check Reddit authentication status")
    subparsers.add_parser("posts", help="List recent submissions")

    p_comments = subparsers.add_parser("comments", help="List comments for a post ID")
    p_comments.add_argument("post_id", help="Submission ID (e.g. 1xgd9d1)")

    p_reply = subparsers.add_parser("reply", help="Reply to a comment")
    p_reply.add_argument("comment_id", help="Comment ID to reply to")
    p_reply.add_argument("text", help="Reply body text")

    p_post_comment = subparsers.add_parser("add-comment", help="Add top-level comment to post")
    p_post_comment.add_argument("post_id", help="Submission ID")
    p_post_comment.add_argument("text", help="Comment text")

    args = parser.parse_args()
    if args.command == "status":
        cmd_status()
    elif args.command == "posts":
        cmd_list_posts()
    elif args.command == "comments":
        cmd_get_comments(args.post_id)
    elif args.command == "reply":
        cmd_reply(args.comment_id, args.text)
    elif args.command == "add-comment":
        cmd_comment_post(args.post_id, args.text)
    else:
        parser.print_help()

if __name__ == "__main__":
    main()
